package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.AttendanceSessionDetailDTO;
import com.example.leavesystem.dto.AttendanceSessionStartRequest;
import com.example.leavesystem.dto.AttendanceSessionStartResponse;
import com.example.leavesystem.dto.AttendanceSessionSummaryDTO;
import com.example.leavesystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.example.leavesystem.dto.StudentCheckinRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤 / 签到相关接口
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 老师发起一次签到场次
     *
     * POST /api/attendance/session/start
     */
    @PostMapping("/session/start")
    public Result<AttendanceSessionStartResponse> startSession(@RequestBody AttendanceSessionStartRequest request) {
        return Result.success(attendanceService.startSession(request));
    }

    /**
     * 老师关闭本次签到场次，并生成缺勤记录
     *
     * POST /api/attendance/session/{sessionId}/close?teacherId=1001
     */
    @PostMapping("/session/{sessionId}/close")
    public Result<Void> closeSession(@PathVariable Long sessionId,
                                     @RequestParam Long teacherId) {
        attendanceService.closeSession(sessionId, teacherId);
        return Result.success(null);
    }

    /**
     * 学生扫码 / 输入口令签到
     *
     * POST /api/attendance/checkin
     */
    @PostMapping("/checkin")
    public Result<Void> checkin(@RequestBody StudentCheckinRequest request) {
        attendanceService.checkin(request);
        return Result.success(null);
    }

    /**
     * 老师查看某段时间内自己的签到场次列表
     *
     * GET /api/attendance/teacher/sessions?teacherId=1001&startDate=2024-10-01&endDate=2024-10-31
     */
    @GetMapping("/teacher/sessions")
    public Result<List<AttendanceSessionSummaryDTO>> teacherSessions(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.listSessionsForTeacher(teacherId, startDate, endDate));
    }

    /**
     * 老师查看某一次签到场次详情
     *
     * GET /api/attendance/session/{sessionId}/detail?teacherId=1001
     */
    @GetMapping("/session/{sessionId}/detail")
    public Result<AttendanceSessionDetailDTO> sessionDetail(@PathVariable Long sessionId,
                                                            @RequestParam Long teacherId) {
        return Result.success(attendanceService.getSessionDetail(sessionId, teacherId));
    }
}
