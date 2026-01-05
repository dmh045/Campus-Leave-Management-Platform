package com.example.leavesystem.security;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfo {
    private Long userId;
    private String userType;
    private String displayName;
    private String roleCode; // e.g., "STUDENT", "COUNSELOR", "TEACHER", "ADMIN"
}

