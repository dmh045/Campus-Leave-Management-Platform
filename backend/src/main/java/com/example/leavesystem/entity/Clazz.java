package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Clazz {

    private Long classId;
    private String classCode;
    private String className;
    private String major;
    private Integer gradeYear;
    private Long counselorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
