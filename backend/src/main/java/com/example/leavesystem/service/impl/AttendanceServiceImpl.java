package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.*;
import com.example.leavesystem.entity.*;
import com.example.leavesystem.mapper.*;
import com.example.leavesystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionMapper attendanceSessionMapper;
    private final StudentCheckinMapper studentCheckinMapper;
    private final OfferingMapper offeringMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;
    private final StudentMapper studentMapper;
    private final AbsenceMapper absenceMapper;
    private final LeaveImpactMapper leaveImpactMapper;

    @Override
    @Transactional
    public AttendanceSessionStartResponse startSession(AttendanceSessionStartRequest request) {
        if (request.getTeacherId() == null || request.getOfferingId() == null) {
            throw new IllegalArgumentException("teacherId 和 offeringId 不能为空");
        }
        if (request.getCourseDate() == null) {
            throw new IllegalArgumentException("courseDate 不能为空");
        }
        if (request.getSectionStart() == null || request.getSectionEnd() == null) {
            throw new IllegalArgumentException("节次范围不能为空");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            request.setDurationMinutes(10);
        }

        Offering offering = offeringMapper.findById(request.getOfferingId());
        if (offering == null) {
            throw new IllegalArgumentException("开课信息不存在");
        }
        if (!Objects.equals(offering.getTeacherId(), request.getTeacherId())) {
            throw new IllegalArgumentException("该开课不属于当前老师");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime allowStartTime = now;
        LocalDateTime allowEndTime = now.plusMinutes(request.getDurationMinutes());
        LocalDateTime tokenExpireTime = allowEndTime;

        String token = generateToken();

        AttendanceSession session = new AttendanceSession();
        session.setTeacherId(request.getTeacherId());
        session.setOfferingId(request.getOfferingId());
        session.setCourseDate(request.getCourseDate());
        session.setSectionStart(request.getSectionStart());
        session.setSectionEnd(request.getSectionEnd());
        session.setToken(token);
        session.setTokenExpireTime(tokenExpireTime);
        session.setAllowStartTime(allowStartTime);
        session.setAllowEndTime(allowEndTime);
        session.setStatus("OPEN");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        attendanceSessionMapper.insert(session);

        return new AttendanceSessionStartResponse(
                session.getSessionId(),
                token,
                allowStartTime,
                allowEndTime,
                tokenExpireTime
        );
    }

    @Override
    @Transactional
    public void closeSession(Long sessionId, Long teacherId) {
        if (sessionId == null || teacherId == null) {
            throw new IllegalArgumentException("sessionId 和 teacherId 不能为空");
        }
        AttendanceSession session = attendanceSessionMapper.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("签到场次不存在");
        }
        if (!Objects.equals(session.getTeacherId(), teacherId)) {
            throw new IllegalArgumentException("不能关闭其他老师的签到场次");
        }
        if ("CLOSED".equalsIgnoreCase(session.getStatus())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        attendanceSessionMapper.updateStatus(sessionId, "CLOSED", now);

        Long offeringId = session.getOfferingId();
        LocalDate courseDate = session.getCourseDate();
        Integer sectionStart = session.getSectionStart();
        Integer sectionEnd = session.getSectionEnd();

        // 1. 应到学生
        List<Long> shouldAttendStudentIds = enrollmentMapper.listStudentIdsByOfferingId(offeringId);
        if (shouldAttendStudentIds.isEmpty()) {
            return;
        }

        // 2. 已签到学生
        List<Long> checkedInStudentIds = studentCheckinMapper.listStudentIdsBySessionId(sessionId);
        Set<Long> checkedInSet = new HashSet<>(checkedInStudentIds);

        // 3. 已请假学生
        List<Long> leaveStudentIds = findApprovedLeaveStudentIds(offeringId, courseDate, sectionStart, sectionEnd);
        Set<Long> leaveSet = new HashSet<>(leaveStudentIds);

        // 4. 生成缺勤记录
        List<Absence> absences = new ArrayList<>();
        for (Long studentId : shouldAttendStudentIds) {
            if (checkedInSet.contains(studentId)) {
                continue;
            }
            if (leaveSet.contains(studentId)) {
                continue;
            }

            Absence absence = new Absence();
            absence.setStudentId(studentId);
            absence.setOfferingId(offeringId);
            absence.setCourseDate(courseDate);
            absence.setSectionStart(sectionStart);
            absence.setSectionEnd(sectionEnd);
            absence.setSource("TEACHER");
            absence.setStatus("PENDING_MAKEUP");
            absence.setCreatedAt(now);
            absence.setUpdatedAt(now);
            absences.add(absence);
        }

        if (!absences.isEmpty()) {
            absenceMapper.insertBatch(absences);
        }
    }

    @Override
    @Transactional
    public void checkin(StudentCheckinRequest request) {
        if (request.getStudentId() == null || request.getToken() == null) {
            throw new IllegalArgumentException("studentId 和 token 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = attendanceSessionMapper.findOpenByToken(request.getToken(), now);
        if (session == null) {
            throw new IllegalArgumentException("无效或已过期的签到token");
        }

        if (now.isBefore(session.getAllowStartTime()) || now.isAfter(session.getAllowEndTime())) {
            throw new IllegalArgumentException("不在签到时间窗口内");
        }

        Long offeringId = session.getOfferingId();
        Long studentId = request.getStudentId();

        int count = enrollmentMapper.countByOfferingIdAndStudentId(offeringId, studentId);
        if (count == 0) {
            throw new IllegalArgumentException("该学生未选修此课程");
        }

        StudentCheckin existed = studentCheckinMapper.findBySessionIdAndStudentId(session.getSessionId(), studentId);
        if (existed != null) {
            // 已经签到过，直接返回
            return;
        }

        StudentCheckin checkin = new StudentCheckin();
        checkin.setSessionId(session.getSessionId());
        checkin.setStudentId(studentId);
        checkin.setCheckinTime(now);
        checkin.setSource("TOKEN");

        studentCheckinMapper.insert(checkin);
    }

    @Override
    public List<AttendanceSessionSummaryDTO> listSessionsForTeacher(Long teacherId, LocalDate startDate, LocalDate endDate) {
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId 不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate 和 endDate 不能为空");
        }

        List<AttendanceSession> sessions = attendanceSessionMapper.listByTeacherAndDateRange(teacherId, startDate, endDate);
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> offeringIds = sessions.stream().map(AttendanceSession::getOfferingId).distinct().collect(Collectors.toList());
        Map<Long, Offering> offeringMap = offeringMapper.findByIds(offeringIds).stream()
                .collect(Collectors.toMap(Offering::getOfferingId, o -> o));

        Map<Long, Course> courseMap = Collections.emptyMap();
        Map<Long, Clazz> clazzMap = Collections.emptyMap();
        if (!offeringMap.isEmpty()) {
            List<Long> courseIds = offeringMap.values().stream().map(Offering::getCourseId).distinct().collect(Collectors.toList());
            List<Long> classIds = offeringMap.values().stream().map(Offering::getClassId).distinct().collect(Collectors.toList());
            courseMap = courseMapper.findByIds(courseIds).stream().collect(Collectors.toMap(Course::getCourseId, c -> c));
            clazzMap = clazzMapper.findByIds(classIds).stream().collect(Collectors.toMap(Clazz::getClassId, c -> c));
        }

        List<AttendanceSessionSummaryDTO> result = new ArrayList<>();
        for (AttendanceSession session : sessions) {
            AttendanceSessionSummaryDTO dto = new AttendanceSessionSummaryDTO();
            dto.setSessionId(session.getSessionId());
            dto.setOfferingId(session.getOfferingId());
            dto.setCourseDate(session.getCourseDate());
            dto.setSectionStart(session.getSectionStart());
            dto.setSectionEnd(session.getSectionEnd());
            dto.setStatus(session.getStatus());
            dto.setAllowStartTime(session.getAllowStartTime());
            dto.setAllowEndTime(session.getAllowEndTime());

            Offering offering = offeringMap.get(session.getOfferingId());
            if (offering != null) {
                Course course = courseMap.get(offering.getCourseId());
                if (course != null) {
                    dto.setCourseName(course.getCourseName());
                }
                Clazz clazz = clazzMap.get(offering.getClassId());
                if (clazz != null) {
                    dto.setClassName(clazz.getClassName());
                }
            }

            int shouldAttend = enrollmentMapper.countByOfferingId(offering.getOfferingId());
            int checkedIn = studentCheckinMapper.countBySessionId(session.getSessionId());
            int leaveCount = findApprovedLeaveStudentIds(
                    session.getOfferingId(),
                    session.getCourseDate(),
                    session.getSectionStart(),
                    session.getSectionEnd()
            ).size();

            dto.setShouldAttendCount(shouldAttend);
            dto.setCheckedInCount(checkedIn);
            dto.setLeaveCount(leaveCount);

            result.add(dto);
        }

        return result;
    }

    @Override
    public AttendanceSessionDetailDTO getSessionDetail(Long sessionId, Long teacherId) {
        if (sessionId == null || teacherId == null) {
            throw new IllegalArgumentException("sessionId 和 teacherId 不能为空");
        }
        AttendanceSession session = attendanceSessionMapper.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("签到场次不存在");
        }
        if (!Objects.equals(session.getTeacherId(), teacherId)) {
            throw new IllegalArgumentException("不能查看其他老师的签到场次详情");
        }

        Long offeringId = session.getOfferingId();
        Offering offering = offeringMapper.findById(offeringId);
        if (offering == null) {
            throw new IllegalArgumentException("开课信息不存在");
        }

        Course course = courseMapper.findById(offering.getCourseId());
        Clazz clazz = clazzMapper.findById(offering.getClassId());

        List<Long> studentIds = enrollmentMapper.listStudentIdsByOfferingId(offeringId);
        if (studentIds.isEmpty()) {
            throw new IllegalArgumentException("本课程没有学生选课");
        }

        Map<Long, Student> studentMap = studentMapper.findByIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s));

        List<StudentCheckin> checkins = studentCheckinMapper.listBySessionId(sessionId);
        Map<Long, StudentCheckin> checkinMap = checkins.stream()
                .collect(Collectors.toMap(StudentCheckin::getStudentId, c -> c));

        List<Long> leaveStudentIds = findApprovedLeaveStudentIds(
                offeringId,
                session.getCourseDate(),
                session.getSectionStart(),
                session.getSectionEnd()
        );
        Set<Long> leaveSet = new HashSet<>(leaveStudentIds);

        AttendanceSessionDetailDTO dto = new AttendanceSessionDetailDTO();
        dto.setSessionId(session.getSessionId());
        dto.setOfferingId(offeringId);
        dto.setCourseDate(session.getCourseDate());
        dto.setSectionStart(session.getSectionStart());
        dto.setSectionEnd(session.getSectionEnd());
        dto.setStatus(session.getStatus());
        dto.setAllowStartTime(session.getAllowStartTime());
        dto.setAllowEndTime(session.getAllowEndTime());
        dto.setCourseName(course != null ? course.getCourseName() : null);
        dto.setClassName(clazz != null ? clazz.getClassName() : null);

        List<AttendanceSessionDetailDTO.StudentStatusItem> studentItems = new ArrayList<>();
        int checkedInCount = 0;
        int leaveCount = 0;
        int absenceCount = 0;

        for (Long studentId : studentIds) {
            Student student = studentMap.get(studentId);
            StudentCheckin checkin = checkinMap.get(studentId);

            AttendanceSessionDetailDTO.StudentStatusItem item = new AttendanceSessionDetailDTO.StudentStatusItem();
            item.setStudentId(studentId);
            if (student != null) {
                item.setStudentNo(student.getStudentNo());
                item.setStudentName(student.getName());
            }

            if (checkin != null) {
                item.setStatus("PRESENT");
                item.setCheckinTime(checkin.getCheckinTime());
                checkedInCount++;
            } else if (leaveSet.contains(studentId)) {
                item.setStatus("LEAVE");
                leaveCount++;
            } else {
                item.setStatus("ABSENT");
                absenceCount++;
            }

            studentItems.add(item);
        }

        dto.setStudents(studentItems);
        dto.setShouldAttendCount(studentIds.size());
        dto.setCheckedInCount(checkedInCount);
        dto.setLeaveCount(leaveCount);
        dto.setAbsenceCount(absenceCount);

        return dto;
    }

    private List<Long> findApprovedLeaveStudentIds(Long offeringId,
                                                   LocalDate courseDate,
                                                   Integer sectionStart,
                                                   Integer sectionEnd) {
        // 在 LeaveImpactMapper 中实现一个多表查询，直接返回符合条件的学生ID
        return leaveImpactMapper.listApprovedStudentIds(offeringId, courseDate, sectionStart, sectionEnd);
    }

    private String generateToken() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
