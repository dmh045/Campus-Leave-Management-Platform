package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.StudentDayCourseDTO;
import com.example.leavesystem.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<List<StudentDayCourseDTO>> studentDay(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<StudentDayCourseDTO> list = timetableService.getStudentDayTimetable(studentId, date);
        return Result.success(list);
    }
}
