package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.LoginRequest;
import com.example.leavesystem.dto.LoginResponse;
import com.example.leavesystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     *
     * POST /api/auth/login
     *
     * Body: { "loginType": "STUDENT"/"STAFF", "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request);
        return Result.success(resp);
    }

    /**
     * 登出接口
     *
     * POST /api/auth/logout?token=xxx
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestParam("token") String token) {
        authService.logout(token);
        return Result.success(null);
    }
}
