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
    @RequiresRoles(value = {"STUDENT", "COUNSELOR", "ADMIN"}, allMatch = false)
    public Result<List<StudentDayCourseDTO>> studentDay(
            @RequestParam(required = false) Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String role = AuthContext.getCurrentRole();

        if ("STUDENT".equalsIgnoreCase(role)) {
            // 学生只看自己：不需要 studentId 参数
            studentId = AuthContext.getCurrentUserId();
        } else {
            // 辅导员/管理员查别人：必须带 studentId
            if (studentId == null) {
                return Result.error(400, "缺少参数 studentId");
            }
        }

        List<StudentDayCourseDTO> list = timetableService.getStudentDayTimetable(studentId, date);
        return Result.success(list);
    }

}
