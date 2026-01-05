package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.AbsenceConvertToLeaveRequest;
import com.example.leavesystem.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    /**
     * 将缺勤记录转为新的请假单
     * POST /api/absences/{absenceId}/convert-to-leave
     */
    @PostMapping("/{absenceId}/convert-to-leave")
    public Result<Long> convertToLeave(@PathVariable("absenceId") Long absenceId,
                                       @RequestBody AbsenceConvertToLeaveRequest request) {
        Long leaveId = absenceService.convertToLeave(absenceId, request);
        return Result.success(leaveId);
    }

    /**
     * 确认缺勤
     * POST /api/absences/{absenceId}/confirm?staffId=1
     */
    @PostMapping("/{absenceId}/confirm")
    public Result<Void> confirm(@PathVariable("absenceId") Long absenceId,
                                @RequestParam("staffId") Long staffId) {
        absenceService.confirmAbsence(absenceId, staffId);
        return Result.success(null);
    }
}
