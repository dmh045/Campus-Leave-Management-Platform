package com.example.leavesystem.dto;


import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String displayName;
    private String userType;
    private String token;
    private String roleCode;
}
