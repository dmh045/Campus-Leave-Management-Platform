package com.example.leavesystem.security;
import java.util.Objects;

public class AuthContext {
    private static final ThreadLocal<AuthInfo> authInfoHolder = new ThreadLocal<>();

    /**
     * 设置当前线程的认证信息
     *
     * @param authInfo 认证信息
     */
    public static void set(AuthInfo authInfo) {
        authInfoHolder.set(authInfo);
    }

    /**
     * 获取当前线程的认证信息
     *
     * @return 认证信息
     */
    public static AuthInfo get() {
        return authInfoHolder.get();
    }

    /**
     * 清除当前线程的认证信息
     */
    public static void clear() {
        authInfoHolder.remove();
    }
    /**
     * 检查当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        return Objects.nonNull(authInfoHolder.get());
    }

    /**
     * 获取当前用户的角色代码
     *
     * @return 角色代码
     */
    public static String getCurrentRole() {
        AuthInfo authInfo = get();
        return authInfo != null ? authInfo.getRoleCode() : null;
    }

    /**
     * 获取当前用户的ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        AuthInfo authInfo = get();
        return authInfo != null ? authInfo.getUserId() : null;
    }

    /**
     * 获取当前用户的类型
     *
     * @return 用户类型
     */
    public static String getCurrentUserType() {
        AuthInfo authInfo = get();
        return authInfo != null ? authInfo.getUserType() : null;
    }
}
