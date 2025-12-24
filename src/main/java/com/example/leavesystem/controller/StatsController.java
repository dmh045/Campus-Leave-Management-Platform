package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
import com.example.leavesystem.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final LeaveService leaveService;

    /**
     * 班级请假统计
     * 示例：
     *   GET /api/stats/class-leave?classId=1&startDate=2024-10-01&endDate=2024-10-31
     */
    @GetMapping("/class-leave")
    public Result<ClassLeaveStatsDTO> classLeaveStats(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ClassLeaveStatsDTO dto = leaveService.getClassLeaveStats(classId, startDate, endDate);
        return Result.success(dto);
    }
}
