package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.CounselorClassDTO;
import com.example.leavesystem.dto.CounselorOfferingDTO;
import com.example.leavesystem.dto.StudentSimpleDTO;
import com.example.leavesystem.entity.Clazz;
import com.example.leavesystem.entity.Course;
import com.example.leavesystem.entity.Offering;
import com.example.leavesystem.entity.Staff;
import com.example.leavesystem.entity.Student;
import com.example.leavesystem.mapper.ClazzMapper;
import com.example.leavesystem.mapper.CourseMapper;
import com.example.leavesystem.mapper.OfferingMapper;
import com.example.leavesystem.mapper.StaffMapper;
import com.example.leavesystem.mapper.StudentMapper;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
@RequiresRoles("COUNSELOR")
public class CounselorDataController {

    private final ClazzMapper clazzMapper;
    private final StudentMapper studentMapper;
    private final OfferingMapper offeringMapper;
    private final CourseMapper courseMapper;
    private final StaffMapper staffMapper;

    private Long counselorId() {
        Long uid = AuthContext.getCurrentUserId();
        if (uid == null) throw new IllegalStateException("未登录");
        return uid;
    }

    private Clazz assertOwnsClass(Long classId) {
        Clazz c = clazzMapper.findById(classId);
        if (c == null) throw new IllegalArgumentException("班级不存在: " + classId);
        if (!Objects.equals(c.getCounselorId(), counselorId())) {
            throw new IllegalStateException("无权访问该班级");
        }
        return c;
    }

    /** GET /api/counselor/classes */
    @GetMapping("/classes")
    public Result<List<CounselorClassDTO>> myClasses() {
        List<Clazz> list = clazzMapper.findByCounselorId(counselorId());
        List<CounselorClassDTO> dto = (list == null ? List.<Clazz>of() : list).stream().map(c -> {
            CounselorClassDTO d = new CounselorClassDTO();
            d.setClassId(c.getClassId());
            d.setClassCode(c.getClassCode());
            d.setClassName(c.getClassName());
            return d;
        }).collect(Collectors.toList());
        return Result.success(dto);
    }

    /** GET /api/counselor/classes/{classId}/students */
    @GetMapping("/classes/{classId}/students")
    public Result<List<StudentSimpleDTO>> classStudents(@PathVariable Long classId) {
        assertOwnsClass(classId);
        List<Student> list = studentMapper.findByClassId(classId);
        List<StudentSimpleDTO> dto = (list == null ? List.<Student>of() : list).stream().map(s -> {
            StudentSimpleDTO d = new StudentSimpleDTO();
            d.setStudentId(s.getStudentId());
            d.setStudentNo(s.getStudentNo());
            d.setName(s.getName());
            d.setClassId(s.getClassId());
            return d;
        }).collect(Collectors.toList());
        return Result.success(dto);
    }

    /** GET /api/counselor/offerings/by-term-class?termId=1&classId=2 */
    @GetMapping("/offerings/by-term-class")
    public Result<List<CounselorOfferingDTO>> offerings(@RequestParam Long termId, @RequestParam Long classId) {
        Clazz clazz = assertOwnsClass(classId);

        List<Offering> offs = offeringMapper.findByTermAndClass(termId, classId);
        if (offs == null || offs.isEmpty()) return Result.success(List.of());

        Set<Long> courseIds = offs.stream().map(Offering::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> teacherIds = offs.stream().map(Offering::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Map.of()
                : courseMapper.findByIds(new ArrayList<>(courseIds)).stream()
                .collect(Collectors.toMap(Course::getCourseId, x -> x));

        Map<Long, Staff> staffMap = teacherIds.isEmpty()
                ? Map.of()
                : staffMapper.findByIds(new ArrayList<>(teacherIds)).stream()
                .collect(Collectors.toMap(Staff::getStaffId, x -> x));

        List<CounselorOfferingDTO> dto = offs.stream().map(o -> {
            CounselorOfferingDTO d = new CounselorOfferingDTO();
            d.setOfferingId(o.getOfferingId());

            d.setCourseId(o.getCourseId());
            Course course = courseMap.get(o.getCourseId());
            d.setCourseName(course != null ? course.getCourseName() : String.valueOf(o.getCourseId()));

            d.setTeacherId(o.getTeacherId());
            Staff staff = staffMap.get(o.getTeacherId());
            d.setTeacherName(staff != null ? staff.getName() : String.valueOf(o.getTeacherId()));

            d.setClassId(o.getClassId());
            d.setClassName(clazz.getClassName());

            d.setWeekDay(o.getWeekDay());
            d.setSectionStart(o.getSectionStart());
            d.setSectionEnd(o.getSectionEnd());
            d.setClassroom(o.getClassroom());
            return d;
        }).collect(Collectors.toList());

        return Result.success(dto);
    }
}
