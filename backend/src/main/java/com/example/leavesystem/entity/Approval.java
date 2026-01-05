package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Approval {

    private Long approvalId;
    private Long leaveId;
    private Long approverId;
    private String approverRole;    // COUNSELOR / TEACHER / SYSTEM
    private String action;          // AGREE / REJECT / RETURN / CANCEL
    private String comment;
    private LocalDateTime createdAt;
}
