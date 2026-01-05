package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假详情 + 时间线 DTO
 */
@Data
public class LeaveDetailDTO {

    // ===== 主表基础信息 =====
    private Long leaveId;
    private Long studentId;
    private String studentName;
    private String className;

    private String leaveType;
    private String applyChannel;
    private String reason;
    private String proofUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // ===== 影响节次列表 =====
    private List<ImpactItem> impacts;

    // ===== 审批记录时间线 =====
    private List<ApprovalItem> approvals;

    @Data
    public static class ImpactItem {
        private Long impactId;
        private Long offeringId;
        private LocalDate courseDate;
        private Integer sectionStart;
        private Integer sectionEnd;

        private String courseName;
        private String teacherName;

        private String confirmStatus;
        private LocalDateTime confirmTime;
        private String remark;
    }

    @Data
    public static class ApprovalItem {
        private Long approvalId;
        private Long approverId;
        private String approverName;
        private String approverRole;   // COUNSELOR / TEACHER / ADMIN ...
        private String action;         // AGREE / REJECT / RETURN 等
        private String comment;
        private LocalDateTime createdAt;
    }
}
