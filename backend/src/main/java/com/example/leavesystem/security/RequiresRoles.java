package com.example.leavesystem.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解，用于标记需要特定角色才能访问的接口
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRoles {

    /**
     * 需要的角色代码数组
     *
     * @return 角色代码数组
     */
    String[] value() default {};

    /**
     * 是否需要所有角色都匹配（默认为false，即只要有一个角色匹配即可）
     *
     * @return 是否需要所有角色都匹配
     */
    boolean allMatch() default false;
}
