package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Staff {

    private Long staffId;
    private String staffNo;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
