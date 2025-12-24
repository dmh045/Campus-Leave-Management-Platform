package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Student {

    private Long studentId;
    private String studentNo;
    private String name;
    private String gender;
    private Long classId;
    private String phone;
    private String email;
    private String status;          // NORMAL / LEAVE / SUSPEND / GRADUATED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
