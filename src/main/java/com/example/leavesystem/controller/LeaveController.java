package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.*;
import com.example.leavesystem.entity.LeaveRequest;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.LeaveService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ================== 安全校验（MOD） ==================

    private Long currentUserId() {
        Long uid = AuthContext.getCurrentUserId();
        if (uid == null) throw new IllegalStateException("未登录");
        return uid;
    }

    private String currentRole() {
        String role = AuthContext.getCurrentRole();
        if (role == null) throw new IllegalStateException("未登录");
        return role;
    }

    /** 防横向越权：参数ID必须等于当前登录用户ID */
    private void requireSelf(Long paramId) {
        Long uid = currentUserId();
        if (paramId != null && !paramId.equals(uid)) {
            throw new IllegalStateException("参数ID与当前登录用户不一致");
        }
    }

    /** 学生端：强制 studentId 以 token 为准 */
    private void fillStudentId(LeaveApplyRequest request) {
        if (!"STUDENT".equalsIgnoreCase(currentRole())) {
            throw new IllegalStateException("仅学生可操作");
        }
        Long uid = currentUserId();
        // 允许前端传，但必须一致
        if (request.getStudentId() != null && !request.getStudentId().equals(uid)) {
            throw new IllegalStateException("studentId 与当前登录用户不一致");
        }
        request.setStudentId(uid);
    }

    // ================== 接口 ==================

    /** 1. 学生发起请假 */
    @PostMapping("/apply")
    @RequiresRoles(value = "STUDENT", allMatch = false) // MOD: 限制为学生
    public Result<LeaveApplyResponse> apply(@RequestBody LeaveApplyRequest request) {
        fillStudentId(request); // MOD: studentId 以 token 为准
        LeaveApplyResponse resp = leaveService.applyLeave(request);
        return Result.success(resp);
    }

    /** 2. 辅导员查看待审批列表 */
    @GetMapping("/pending/counselor")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<List<CounselorPendingLeaveDTO>> pendingForCounselor(@RequestParam Long counselorId) {
        requireSelf(counselorId); // MOD: counselorId 必须是本人
        return Result.success(leaveService.listPendingForCounselor(counselorId));
    }

    /** 3. 辅导员审批（同意 / 拒绝 / 退回） */
    @PostMapping("/{id}/counselor-approve")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<Void> counselorApprove(@PathVariable("id") Long leaveId,
                                         @RequestBody CounselorApproveRequest request) {
        requireSelf(request.getCounselorId()); // MOD
        leaveService.counselorApprove(
                leaveId,
                request.getCounselorId(),
                request.getAction(),
                request.getComment()
        );
        return Result.success(null);
    }

    /** 4. 任课教师查看自己的待确认节次 */
    @GetMapping("/pending/teacher")
    @RequiresRoles(value = "TEACHER", allMatch = false) // MOD: 限制为教师
    public Result<List<TeacherPendingImpactDTO>> pendingForTeacher(@RequestParam Long teacherId) {
        requireSelf(teacherId); // MOD
        return Result.success(leaveService.listPendingForTeacher(teacherId));
    }

    /** 5. 任课教师确认某一节次请假 */
    @PostMapping("/impact/{impactId}/teacher-confirm")
    @RequiresRoles(value = "TEACHER", allMatch = false) // MOD: 限制为教师
    public Result<Void> teacherConfirm(@PathVariable Long impactId,
                                       @RequestBody TeacherConfirmRequest request) {
        requireSelf(request.getTeacherId()); // MOD
        leaveService.teacherConfirmImpact(impactId, request.getTeacherId(), request.getRemark());
        return Result.success(null);
    }

    /** 6. 学生查看自己的请假列表 */
    @GetMapping("/my")
    @RequiresRoles(value = "STUDENT", allMatch = false) // MOD: 限制为学生
    public Result<List<LeaveRequest>> myLeaves(@RequestParam Long studentId) {
        requireSelf(studentId); // MOD
        return Result.success(leaveService.listLeavesForStudent(studentId));
    }

    /** 7. 请假详情 + 时间线 */
    @GetMapping("/{id}/detail")
    @RequiresRoles // 只要求登录；具体能否看由 service 再校验更稳（可选）
    public Result<LeaveDetailDTO> detail(@PathVariable("id") Long leaveId) {
        return Result.success(leaveService.getLeaveDetail(leaveId));
    }

    /** 8. 辅导员批量审批 */
    @PostMapping("/counselor-approve/batch")
    @RequiresRoles(value = "COUNSELOR", allMatch = false) // MOD: 限制为辅导员
    public Result<Integer> counselorApproveBatch(@RequestBody CounselorBatchApproveRequest request) {
        requireSelf(request.getCounselorId()); // MOD
        leaveService.counselorBatchApprove(
                request.getCounselorId(),
                request.getAction(),
                request.getComment(),
                request.getLeaveIds()
        );
        return Result.success(request.getLeaveIds() != null ? request.getLeaveIds().size() : 0);
    }

    /** 9. 辅导员批量发起公假 */
    @PostMapping("/public/batch")
    @RequiresRoles(value = "COUNSELOR", allMatch = false) // MOD: 限制为辅导员
    public Result<List<Long>> createPublicLeave(@RequestBody PublicLeaveBatchRequest request) {
        requireSelf(request.getCounselorId()); // MOD
        return Result.success(leaveService.createPublicLeaves(request));
    }

    /** 10. 学生重新提交被退回的请假单 */
    @PutMapping("/{id}/resubmit")
    @RequiresRoles(value = "STUDENT", allMatch = false)
    public Result<LeaveApplyResponse> resubmit(@PathVariable("id") Long leaveId,
                                               @RequestBody LeaveApplyRequest request) {
        fillStudentId(request); // MOD: studentId 以 token 为准
        return Result.success(leaveService.resubmitLeave(leaveId, request));
    }

    /** 11. 任课教师按课程维度查看待确认请假 */
    @GetMapping("/pending/teacher/by-course")
    @RequiresRoles(value = "TEACHER", allMatch = false) // MOD: 限制为教师
    public Result<List<CoursePendingImpactDTO>> pendingByCourseForTeacher(@RequestParam Long teacherId) {
        requireSelf(teacherId); // MOD
        return Result.success(leaveService.listPendingByCourseForTeacher(teacherId));
    }

    // ====== 请求体 DTO ======

    @Data
    public static class CounselorBatchApproveRequest {
        private Long counselorId;
        private String action;          // AGREE / REJECT / RETURN
        private String comment;
        private List<Long> leaveIds;
    }

    @Data
    public static class CounselorApproveRequest {
        private Long counselorId;
        private String action;          // AGREE / REJECT / RETURN
        private String comment;
    }

    @Data
    public static class TeacherConfirmRequest {
        private Long teacherId;
        private String remark;
    }
}
