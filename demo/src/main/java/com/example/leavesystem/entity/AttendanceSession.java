package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceSession {

    private Long sessionId;
    private Long teacherId;
    private Long offeringId;

    private LocalDate courseDate;
    private Integer sectionStart;
    private Integer sectionEnd;

    private String token;
    private LocalDateTime tokenExpireTime;

    private LocalDateTime allowStartTime;
    private LocalDateTime allowEndTime;

    private String status; // OPEN / CLOSED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
