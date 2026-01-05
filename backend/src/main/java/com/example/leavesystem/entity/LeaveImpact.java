package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveImpact {

    private Long impactId;
    private Long leaveId;
    private Long offeringId;
    private LocalDate courseDate;
    private Integer sectionStart;
    private Integer sectionEnd;
    private Long teacherId;
    private String confirmStatus;   // PENDING / CONFIRMED
    private LocalDateTime confirmTime;
    private String remark;
}
