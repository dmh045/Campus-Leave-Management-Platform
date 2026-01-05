package com.example.leavesystem.security;

import com.example.leavesystem.entity.LoginToken;
import com.example.leavesystem.mapper.LoginTokenMapper;
import com.example.leavesystem.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 认证拦截器，用于验证请求的token有效性
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final LoginTokenMapper loginTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头或参数中获取token
        String token = getTokenFromRequest(request);

        if (token == null) {
            sendErrorResponse(response, 401, "未提供认证信息");
            return false;
        }

        // 验证token的有效性
        LoginToken loginToken = loginTokenMapper.findValid(token);
        if (loginToken == null) {
            sendErrorResponse(response, 401, "认证信息无效或已过期");
            return false;
        }

        // 将认证信息存储到上下文
        AuthInfo authInfo = new AuthInfo();
        authInfo.setUserType(loginToken.getUserType());
        authInfo.setUserId(loginToken.getUserId());
        authInfo.setRoleCode(loginToken.getRoleCode());
        // 注意：这里没有displayName，因为LoginToken中没有存储，可以根据需要从数据库查询
        AuthContext.set(authInfo);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthContext.clear();
    }

    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 首先从请求头获取
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        // 如果请求头没有，则从请求参数获取
        return request.getParameter("token");
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            Result<Void> result = Result.error(code, message);
            writer.write(objectMapper.writeValueAsString(result));
            writer.flush();
        }
    }
}