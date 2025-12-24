package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CounselorPendingLeaveDTO {

    private Long leaveId;
    private Long studentId;
    private String studentName;
    private String className;
    private String leaveType;
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
