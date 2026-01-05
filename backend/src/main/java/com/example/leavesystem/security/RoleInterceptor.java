package com.example.leavesystem.security;

import com.example.leavesystem.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * 角色权限拦截器，用于验证用户是否具有访问接口所需的角色
 */
@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查处理器是否是HandlerMethod
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 获取方法上的RequiresRoles注解
        RequiresRoles rolesAnnotation = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        if (rolesAnnotation == null) {
            // 如果方法上没有，则获取类上的
            rolesAnnotation = handlerMethod.getBeanType().getAnnotation(RequiresRoles.class);
        }

        // 如果没有RequiresRoles注解，则允许访问
        if (rolesAnnotation == null) {
            return true;
        }

        // 检查用户是否已登录
        if (!AuthContext.isAuthenticated()) {
            sendErrorResponse(response, 401, "未登录");
            return false;
        }

        // 获取用户的角色
        String userRole = AuthContext.getCurrentRole();

        // 获取需要的角色
        String[] requiredRoles = rolesAnnotation.value();
        if (requiredRoles.length == 0) {
            // 如果没有指定需要的角色，则允许访问
            return true;
        }

        // 检查用户是否具有需要的角色
        boolean hasRole = false;
        if (rolesAnnotation.allMatch()) {
            // 需要所有角色都匹配
            hasRole = Arrays.stream(requiredRoles).allMatch(role -> role.equals(userRole));
        } else {
            // 只要有一个角色匹配即可
            hasRole = Arrays.stream(requiredRoles).anyMatch(role -> role.equals(userRole));
        }

        if (!hasRole) {
            sendErrorResponse(response, 403, "没有访问权限");
            return false;
        }

        return true;
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