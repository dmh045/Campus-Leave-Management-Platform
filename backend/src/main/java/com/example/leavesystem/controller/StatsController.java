package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
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
    @RequiresRoles(value = {"COUNSELOR", "ADMIN"}, allMatch = false) // MOD: 仅辅导员/管理员
    public Result<ClassLeaveStatsDTO> classLeaveStats(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // MOD(可选增强)：辅导员只能查自己负责的班级
        // String role = AuthContext.getCurrentRole();
        // if ("COUNSELOR".equalsIgnoreCase(role)) {
        //     Long counselorId = AuthContext.getCurrentUserId();
        //     boolean ok = leaveService.isCounselorOfClass(counselorId, classId);
        //     if (!ok) throw new IllegalStateException("无权限查看该班级统计");
        // }

        ClassLeaveStatsDTO dto = leaveService.getClassLeaveStats(classId, startDate, endDate);
        return Result.success(dto);
    }
}
