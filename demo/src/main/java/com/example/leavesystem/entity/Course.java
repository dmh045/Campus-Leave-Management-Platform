package com.example.leavesystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Course {

    private Long courseId;
    private String courseCode;
    private String courseName;
    private BigDecimal credit;
    private Integer totalHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
