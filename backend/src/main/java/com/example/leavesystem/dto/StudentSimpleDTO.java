package com.example.leavesystem.dto;

import lombok.Data;

@Data
public class StudentSimpleDTO {
    private Long studentId;
    private String studentNo;
    private String name;
    private Long classId;
}
