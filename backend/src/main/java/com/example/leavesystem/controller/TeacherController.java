package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.*;
import com.example.leavesystem.mapper.*;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final OfferingMapper offeringMapper;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;

    private final AttendanceSessionMapper attendanceSessionMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final StudentMapper studentMapper;
    private final StudentCheckinMapper studentCheckinMapper;
    private final LeaveImpactMapper leaveImpactMapper;

    // ===== 1) 给前端下拉框用：老师的课程-班级列表 =====
    @GetMapping("/courses")
    @RequiresRoles(value = "TEACHER", allMatch = false)
    public Result<List<TeacherCourseDTO>> getTeacherCourses() {
        Long teacherId = mustLoginTeacherId();

        List<Offering> offerings = offeringMapper.findByTeacherId(teacherId);
        if (offerings.isEmpty()) return Result.success(Collections.emptyList());

        List<Long> courseIds = offerings.stream().map(Offering::getCourseId).distinct().collect(Collectors.toList());
        List<Long> classIds  = offerings.stream().map(Offering::getClassId).distinct().collect(Collectors.toList());

        Map<Long, Course> courseMap = courseMapper.findByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        Map<Long, Clazz> classMap = clazzMapper.findByIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getClassId, c -> c));

        // 去重输出：同一 courseId + classId 只输出一次
        Set<String> seen = new HashSet<>();
        List<TeacherCourseDTO> out = new ArrayList<>();
        for (Offering o : offerings) {
            String key = o.getCourseId() + "_" + o.getClassId();
            if (!seen.add(key)) continue;

            Course co = courseMap.get(o.getCourseId());
            Clazz clz = classMap.get(o.getClassId());

            TeacherCourseDTO dto = new TeacherCourseDTO();
            dto.setCourseId(o.getCourseId());
            dto.setCourseName(co != null ? co.getCourseName() : null);
            dto.setClassId(o.getClassId());
            dto.setClassName(clz != null ? clz.getClassName() : null);
            out.add(dto);
        }
        return Result.success(out);
    }

    // ===== 2) 导出老师考勤 Excel（注意：这里不能用 Result 包裹，必须直接返回文件流）=====
    @GetMapping("/attendance/export")
    @RequiresRoles(value = "TEACHER", allMatch = false)
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer fullScore,
            @RequestParam(required = false) Integer leaveScore,
            @RequestParam(required = false) Integer absentScore
    ) throws Exception {

        Long teacherId = mustLoginTeacherId();

        int fs = (fullScore == null ? 10 : fullScore);
        int ls = (leaveScore == null ? 0 : leaveScore);   // 默认不扣请假
        int as = (absentScore == null ? 1 : absentScore); // 默认缺勤扣1

        List<AttendanceSession> sessions = attendanceSessionMapper.listByTeacherAndDateRange(teacherId, startDate, endDate);
        if (sessions.isEmpty()) {
            return buildEmptyExcel("考勤统计表_空数据.xlsx");
        }

        // 过滤：只导出 CLOSED 更合理（可按你需求改）
        sessions = sessions.stream().filter(s -> "CLOSED".equalsIgnoreCase(s.getStatus())).toList();
        if (sessions.isEmpty()) return buildEmptyExcel("考勤统计表_无已关闭场次.xlsx");

        // 先拿 offeringMap，用来按 courseId/classId 过滤 + 获取课程班级信息
        List<Long> offeringIds = sessions.stream().map(AttendanceSession::getOfferingId).distinct().toList();
        List<Offering> offeringList = offeringMapper.findByIds(offeringIds);
        Map<Long, Offering> offeringMap = offeringList.stream().collect(Collectors.toMap(Offering::getOfferingId, o -> o));

        // 课程/班级过滤
        sessions = sessions.stream().filter(s -> {
            Offering o = offeringMap.get(s.getOfferingId());
            if (o == null) return false;
            if (courseId != null && !courseId.equals(o.getCourseId())) return false;
            if (classId  != null && !classId.equals(o.getClassId()))  return false;
            return true;
        }).toList();

        if (sessions.isEmpty()) return buildEmptyExcel("考勤统计表_筛选无数据.xlsx");

        // 预加载 course/class
        List<Long> courseIds = sessions.stream().map(s -> offeringMap.get(s.getOfferingId()).getCourseId()).distinct().toList();
        List<Long> classIds  = sessions.stream().map(s -> offeringMap.get(s.getOfferingId()).getClassId()).distinct().toList();
        Map<Long, Course> courseMap = courseMapper.findByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        Map<Long, Clazz> classMap = clazzMapper.findByIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getClassId, c -> c));

        // 聚合结构：key = courseId_classId_studentId（这样跨时间段统计）
        class Agg {
            Long courseId, classId, studentId;
            String courseName, className, studentNo, studentName;
            int present, leave, absent;
        }
        Map<String, Agg> aggMap = new HashMap<>();

        // 缓存：避免重复查
        Map<Long, List<Long>> offeringStudentsCache = new HashMap<>();

        Set<Long> allStudentIds = new HashSet<>();

        for (AttendanceSession s : sessions) {
            Offering o = offeringMap.get(s.getOfferingId());
            if (o == null) continue;

            List<Long> shouldIds = offeringStudentsCache.computeIfAbsent(o.getOfferingId(),
                    k -> enrollmentMapper.listStudentIdsByOfferingId(k));
            if (shouldIds.isEmpty()) continue;

            Set<Long> checkedIn = new HashSet<>(studentCheckinMapper.listStudentIdsBySessionId(s.getSessionId()));
            Set<Long> leaveSet  = new HashSet<>(leaveImpactMapper.listApprovedStudentIds(
                    o.getOfferingId(), s.getCourseDate(), s.getSectionStart(), s.getSectionEnd()));

            for (Long sid : shouldIds) {
                allStudentIds.add(sid);
                String key = o.getCourseId() + "_" + o.getClassId() + "_" + sid;

                Agg a = aggMap.computeIfAbsent(key, kk -> {
                    Agg x = new Agg();
                    x.courseId = o.getCourseId();
                    x.classId = o.getClassId();
                    x.studentId = sid;
                    Course co = courseMap.get(o.getCourseId());
                    Clazz clz = classMap.get(o.getClassId());
                    x.courseName = co != null ? co.getCourseName() : "";
                    x.className  = clz != null ? clz.getClassName() : "";
                    return x;
                });

                if (checkedIn.contains(sid)) a.present++;
                else if (leaveSet.contains(sid)) a.leave++;
                else a.absent++;
            }
        }

        // 补学生信息
        Map<Long, Student> studentMap = studentMapper.findByIds(new ArrayList<>(allStudentIds)).stream()
                .collect(Collectors.toMap(Student::getStudentId, st -> st));

        for (Agg a : aggMap.values()) {
            Student st = studentMap.get(a.studentId);
            if (st != null) {
                a.studentNo = st.getStudentNo();
                a.studentName = st.getName();
            } else {
                a.studentNo = "";
                a.studentName = "";
            }
        }

        // 生成 Excel
        byte[] bytes;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("考勤表");

            int r = 0;
            Row header = sheet.createRow(r++);
            String[] cols = {"班级", "课程", "学号", "姓名", "到课次数", "请假次数", "缺勤次数", "考勤分数"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            List<Agg> rows = new ArrayList<>(aggMap.values());
            rows.sort(Comparator
                    .comparing((Agg x) -> x.className, Comparator.nullsLast(String::compareTo))
                    .thenComparing(x -> x.courseName, Comparator.nullsLast(String::compareTo))
                    .thenComparing(x -> x.studentNo, Comparator.nullsLast(String::compareTo)));

            for (Agg a : rows) {
                Row row = sheet.createRow(r++);
                int score = fs - a.absent * as - a.leave * ls;
                if (score < 0) score = 0;

                row.createCell(0).setCellValue(a.className);
                row.createCell(1).setCellValue(a.courseName);
                row.createCell(2).setCellValue(a.studentNo);
                row.createCell(3).setCellValue(a.studentName);
                row.createCell(4).setCellValue(a.present);
                row.createCell(5).setCellValue(a.leave);
                row.createCell(6).setCellValue(a.absent);
                row.createCell(7).setCellValue(score);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(bos);
            bytes = bos.toByteArray();
        }

        String filename = "考勤统计表_" + startDate + "_" + endDate + ".xlsx";
        if (classId != null) filename = "考勤统计表_" + classId + "_" + startDate + "_" + endDate + ".xlsx";

        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    private Long mustLoginTeacherId() {
        Long uid = AuthContext.getCurrentUserId();
        if (uid == null) throw new IllegalStateException("未登录");
        return uid;
    }

    private ResponseEntity<byte[]> buildEmptyExcel(String filename) throws Exception {
        byte[] bytes;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("考勤表");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("暂无数据");
            wb.write(bos);
            bytes = bos.toByteArray();
        }
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @Data
    public static class TeacherCourseDTO {
        private Long courseId;
        private String courseName;
        private Long classId;
        private String className;
    }
}
