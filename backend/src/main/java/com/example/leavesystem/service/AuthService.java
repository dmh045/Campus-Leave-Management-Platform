package com.example.leavesystem.service;
import com.example.leavesystem.dto.LoginResponse;
import com.example.leavesystem.dto.LoginRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String token);;
}
