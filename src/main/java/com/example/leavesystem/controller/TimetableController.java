package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.StudentDayCourseDTO;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * 学生某一天课表
     * 示例：GET /api/timetable/student/day?studentId=1&date=2024-10-10
     */
    @GetMapping("/student/day")
    @RequiresRoles(value = {"STUDENT", "COUNSELOR", "ADMIN"}, allMatch = false) // MOD: 至少登录，允许这三类
    public Result<List<StudentDayCourseDTO>> studentDay(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // MOD: 学生只能查看自己的课表（防横向越权）
        String role = AuthContext.getCurrentRole();
        if ("STUDENT".equalsIgnoreCase(role)) {
            Long currentId = AuthContext.getCurrentUserId();
            if (studentId != null && !studentId.equals(currentId)) {
                throw new IllegalStateException("无权限查看其他学生课表");
            }
            studentId = currentId; // 强制以登录态为准
        }

        // MOD(可选增强)：辅导员只能查自己负责班级的学生
        // if ("COUNSELOR".equalsIgnoreCase(role)) {
        //     Long counselorId = AuthContext.getCurrentUserId();
        //     boolean ok = timetableService.canCounselorViewStudent(counselorId, studentId);
        //     if (!ok) throw new IllegalStateException("无权限查看该学生课表");
        // }

        List<StudentDayCourseDTO> list = timetableService.getStudentDayTimetable(studentId, date);
        return Result.success(list);
    }
}
