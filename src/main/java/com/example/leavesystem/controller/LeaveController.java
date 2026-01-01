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
// ✅ 兼容前端误写的 /api/leave/**（单数）以及你现在的 /api/leaves/**（复数）
@RequestMapping({"/api/leaves", "/api/leave"})
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ================== 安全校验 ==================

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

    /** 防横向越权：参数ID必须等于当前登录用户ID；若没传则自动用当前用户ID */
    private Long selfIdOrCurrent(Long paramId, String paramName) {
        Long uid = currentUserId();
        if (paramId == null) return uid;
        if (!paramId.equals(uid)) {
            throw new IllegalStateException(paramName + " 与当前登录用户不一致");
        }
        return paramId;
    }

    /** 学生端：强制 studentId 以 token 为准 */
    private void fillStudentId(LeaveApplyRequest request) {
        if (!"STUDENT".equalsIgnoreCase(currentRole())) {
            throw new IllegalStateException("仅学生可操作");
        }
        Long uid = currentUserId();
        if (request.getStudentId() != null && !request.getStudentId().equals(uid)) {
            throw new IllegalStateException("studentId 与当前登录用户不一致");
        }
        request.setStudentId(uid);
    }

    // ================== 接口 ==================

    /** 1. 学生发起请假 */
    @PostMapping("/apply")
    @RequiresRoles(value = "STUDENT", allMatch = false)
    public Result<LeaveApplyResponse> apply(@RequestBody LeaveApplyRequest request) {
        fillStudentId(request);
        return Result.success(leaveService.applyLeave(request));
    }

    /** 2. 辅导员查看待审批列表（counselorId 可不传，默认取 token） */
    @GetMapping("/pending/counselor")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<List<CounselorPendingLeaveDTO>> pendingForCounselor(
            @RequestParam(required = false) Long counselorId
    ) {
        counselorId = selfIdOrCurrent(counselorId, "counselorId");
        return Result.success(leaveService.listPendingForCounselor(counselorId));
    }

    /** 3. 辅导员审批（同意 / 拒绝 / 退回）（request.counselorId 可不传，默认取 token） */
    @PostMapping("/{id}/counselor-approve")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<Void> counselorApprove(@PathVariable("id") Long leaveId,
                                         @RequestBody CounselorApproveRequest request) {
        Long counselorId = currentUserId(); // ✅ 从 token 来
        leaveService.counselorApprove(
                leaveId,
                counselorId,
                request.getComment(),
                request.getAction()
        );
        return Result.success(null);
    }

    /** 4. 任课教师查看自己的待确认节次（teacherId 可不传，默认取 token） */
    @GetMapping("/pending/teacher")
    @RequiresRoles(value = "TEACHER", allMatch = false)
    public Result<List<TeacherPendingImpactDTO>> pendingForTeacher(
            @RequestParam(required = false) Long teacherId
    ) {
        teacherId = selfIdOrCurrent(teacherId, "teacherId");
        return Result.success(leaveService.listPendingForTeacher(teacherId));
    }

    /** 5. 任课教师确认某一节次请假（request.teacherId 可不传，默认取 token） */
    @PostMapping("/impact/{impactId}/teacher-confirm")
    @RequiresRoles(value = "TEACHER", allMatch = false)
    public Result<Void> teacherConfirm(@PathVariable Long impactId,
                                       @RequestBody TeacherConfirmRequest request) {
        Long teacherId = selfIdOrCurrent(request.getTeacherId(), "teacherId");
        request.setTeacherId(teacherId);

        leaveService.teacherConfirmImpact(impactId, teacherId, request.getRemark());
        return Result.success(null);
    }

    /** 6. 学生查看自己的请假列表
     *  ✅ 兼容：
     *   - GET /api/leaves/my
     *   - GET /api/leaves/list
     *   - GET /api/leave/list   (前端误写也能通)
     *  studentId 可不传，默认取 token
     */
    @GetMapping({"/my", "/list"})
    @RequiresRoles(value = "STUDENT", allMatch = false)
    public Result<List<LeaveRequest>> myLeaves(@RequestParam(required = false) Long studentId) {
        studentId = selfIdOrCurrent(studentId, "studentId");
        return Result.success(leaveService.listLeavesForStudent(studentId));
    }

    /** 7. 请假详情 + 时间线 */
    @GetMapping("/{id}/detail")
    @RequiresRoles
    public Result<LeaveDetailDTO> detail(@PathVariable("id") Long leaveId) {
        return Result.success(leaveService.getLeaveDetail(leaveId));
    }

    /** 8. 辅导员批量审批（request.counselorId 可不传，默认取 token） */
    @PostMapping("/counselor-approve/batch")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<Integer> counselorApproveBatch(@RequestBody CounselorBatchApproveRequest request) {
        Long counselorId = selfIdOrCurrent(request.getCounselorId(), "counselorId");
        request.setCounselorId(counselorId);

        leaveService.counselorBatchApprove(
                counselorId,
                request.getAction(),
                request.getComment(),
                request.getLeaveIds()
        );
        return Result.success(request.getLeaveIds() != null ? request.getLeaveIds().size() : 0);
    }

    /** 9. 辅导员批量发起公假（request.counselorId 可不传，默认取 token） */
    @PostMapping("/public/batch")
    @RequiresRoles(value = "COUNSELOR", allMatch = false)
    public Result<List<Long>> createPublicLeave(@RequestBody PublicLeaveBatchRequest request) {
        Long counselorId = selfIdOrCurrent(request.getCounselorId(), "counselorId");
        request.setCounselorId(counselorId);
        return Result.success(leaveService.createPublicLeaves(request));
    }

    /** 10. 学生重新提交被退回的请假单 */
    @PutMapping("/{id}/resubmit")
    @RequiresRoles(value = "STUDENT", allMatch = false)
    public Result<LeaveApplyResponse> resubmit(@PathVariable("id") Long leaveId,
                                               @RequestBody LeaveApplyRequest request) {
        fillStudentId(request);
        return Result.success(leaveService.resubmitLeave(leaveId, request));
    }

    /** 11. 任课教师按课程维度查看待确认请假（teacherId 可不传，默认取 token） */
    @GetMapping("/pending/teacher/by-course")
    @RequiresRoles(value = "TEACHER", allMatch = false)
    public Result<List<CoursePendingImpactDTO>> pendingByCourseForTeacher(
            @RequestParam(required = false) Long teacherId
    ) {
        teacherId = selfIdOrCurrent(teacherId, "teacherId");
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
