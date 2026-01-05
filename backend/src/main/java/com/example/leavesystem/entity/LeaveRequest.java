package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequest {

    private Long leaveId;
    private Long studentId;
    private Long termId;
    private String leaveType;       // SICK / PERSONAL / PUBLIC
    private String applyChannel;    // BY_COURSE / BY_TIME
    private String reason;
    private String proofUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;          // DRAFT / PENDING_COUNSELOR / PENDING_TEACHER / APPROVED / REJECTED ...
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
