package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Absence {

    private Long absenceId;
    private Long studentId;
    private Long offeringId;
    private LocalDate courseDate;
    private Integer sectionStart;
    private Integer sectionEnd;
    private String source;          // TEACHER / IMPORT
    private String status;          // PENDING_MAKEUP / CONFIRMED / CONVERTED_TO_LEAVE
    private LocalDateTime makeupDeadline;
    private Long convertedLeaveId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
