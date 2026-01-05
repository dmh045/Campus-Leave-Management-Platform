package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Enrollment {

    private Long enrollmentId;
    private Long studentId;
    private Long offeringId;
    private String status;          // ENROLLED / DROPPED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
