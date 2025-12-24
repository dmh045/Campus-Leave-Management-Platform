package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginToken {

    private Long tokenId;

    private String userType;     // STUDENT / STAFF
    private Long userId;
    private String roleCode;     // STAFF 对应 staff_role.role_code，学生可写 STUDENT

    private String token;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
}
