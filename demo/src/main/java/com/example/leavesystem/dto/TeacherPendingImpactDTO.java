package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherPendingImpactDTO {
    private Long impactId;
    private Long leaveId;
    private Long studentId;
    private String studentName;
    private String className;
    private String courseName;
    private LocalDate courseDate;
    private Integer sectionStart;
    private Integer sectionEnd;
    private String leaveType;
    private String reason;
    private String leaveStatus;
}
