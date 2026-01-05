package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentCheckin {

    private Long checkinId;
    private Long sessionId;
    private Long studentId;
    private LocalDateTime checkinTime;
    private String source;
}
