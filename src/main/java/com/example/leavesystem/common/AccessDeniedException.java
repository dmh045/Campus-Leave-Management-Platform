package com.example.leavesystem.common;

/**
 * 统一的权限异常（用于控制器/服务层主动抛出 403）
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
