package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.CounselorPendingLeaveDTO;
import com.example.leavesystem.dto.LeaveApplyRequest;
import com.example.leavesystem.dto.LeaveApplyResponse;
import com.example.leavesystem.dto.TeacherPendingImpactDTO;
import com.example.leavesystem.dto.CoursePendingImpactDTO;
import com.example.leavesystem.entity.LeaveRequest;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.LeaveService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.leavesystem.dto.LeaveDetailDTO;
import com.example.leavesystem.dto.PublicLeaveBatchRequest;
import com.example.leavesystem.dto.ClassLeaveStatsDTO;


import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * 1. 学生发起请假
     */
    @PostMapping("/apply")
    @RequiresRoles
    public Result<LeaveApplyResponse> apply(@RequestBody LeaveApplyRequest request) {
        LeaveApplyResponse resp = leaveService.applyLeave(request);
        return Result.success(resp);
    }

    /**
     * 2. 辅导员查看待审批列表
     */
    @GetMapping("/pending/counselor")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<List<CounselorPendingLeaveDTO>> pendingForCounselor(@RequestParam Long counselorId) {
        List<CounselorPendingLeaveDTO> list = leaveService.listPendingForCounselor(counselorId);
        return Result.success(list);
    }

    /**
     * 3. 辅导员审批（同意 / 拒绝）
     */
    @PostMapping("/{id}/counselor-approve")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<Void> counselorApprove(@PathVariable("id") Long leaveId,
                                         @RequestBody CounselorApproveRequest request) {
        leaveService.counselorApprove(
                leaveId,
                request.getCounselorId(),
                request.getComment(),
                request.getAction()
        );
        return Result.success(null);
    }

    /**
     * 4. 任课教师查看自己的待确认节次
     */
    @GetMapping("/pending/teacher")
    @RequiresRoles
    public Result<List<TeacherPendingImpactDTO>> pendingForTeacher(@RequestParam Long teacherId) {
        List<TeacherPendingImpactDTO> list = leaveService.listPendingForTeacher(teacherId);
        return Result.success(list);
    }

    /**
     * 5. 任课教师确认某一节次请假
     */
    @PostMapping("/impact/{impactId}/teacher-confirm")
    @RequiresRoles
    public Result<Void> teacherConfirm(@PathVariable Long impactId,
                                       @RequestBody TeacherConfirmRequest request) {
        leaveService.teacherConfirmImpact(impactId, request.getTeacherId(), request.getRemark());
        return Result.success(null);
    }

    /**
     * 6. 学生查看自己的请假列表
     */
    @GetMapping("/my")
    @RequiresRoles
    public Result<List<LeaveRequest>> myLeaves(@RequestParam Long studentId) {
        return Result.success(leaveService.listLeavesForStudent(studentId));
    }

    /**
     * 7. 请假详情 + 时间线
     */
    @GetMapping("/{id}/detail")
    @RequiresRoles
    public Result<LeaveDetailDTO> detail(@PathVariable("id") Long leaveId) {
        return Result.success(leaveService.getLeaveDetail(leaveId));
    }

    /**
     * 8.辅导员批量审批
     */
    @PostMapping("/counselor-approve/batch")
    @RequiresRoles
    public Result<Integer> counselorApproveBatch(@RequestBody CounselorBatchApproveRequest request) {
        leaveService.counselorBatchApprove(
                request.getCounselorId(),
                request.getAction(),
                request.getComment(),
                request.getLeaveIds()
        );
        return Result.success(request.getLeaveIds() != null ? request.getLeaveIds().size() : 0);
    }

    @Data
    public static class CounselorBatchApproveRequest {
        private Long counselorId;
        private String action;          // AGREE / REJECT / RETURN
        private String comment;
        private java.util.List<Long> leaveIds;
    }

    /**
     * 9.辅导员批量发起公假
     */
    @PostMapping("/public/batch")
    @RequiresRoles
    public Result<java.util.List<Long>> createPublicLeave(@RequestBody PublicLeaveBatchRequest request) {
        java.util.List<Long> ids = leaveService.createPublicLeaves(request);
        return Result.success(ids);
    }
    /**
     * 10. 学生重新提交被退回的请假单
     */
    @PutMapping("/{id}/resubmit")
    @RequiresRoles(value = "STUDENT", allMatch = false)
    public Result<LeaveApplyResponse> resubmit(@PathVariable("id") Long leaveId,
                                               @RequestBody LeaveApplyRequest request) {
        LeaveApplyResponse resp = leaveService.resubmitLeave(leaveId, request);
        return Result.success(resp);
    }
    /**
     * 11. 任课教师按课程维度查看待确认请假
     */
    @GetMapping("/pending/teacher/by-course")
    @RequiresRoles
    public Result<List<CoursePendingImpactDTO>> pendingByCourseForTeacher(@RequestParam Long teacherId) {
        List<CoursePendingImpactDTO> list = leaveService.listPendingByCourseForTeacher(teacherId);
        return Result.success(list);
    }

    // ====== 请求体 DTO ======

    @Data
    public static class CounselorApproveRequest {
        private Long counselorId;
        /**
         * 审批动作：AGREE / REJECT / RETURN
         */
        private String action;
        private String comment;
    }


    @Data
    public static class TeacherConfirmRequest {
        private Long teacherId;
        private String remark;
    }
}