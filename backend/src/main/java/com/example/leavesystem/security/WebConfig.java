package com.example.leavesystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.leavesystem.mapper.LoginTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于注册拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTokenMapper loginTokenMapper;

    /**
     * 创建ObjectMapper Bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * 创建AuthInterceptor Bean
     */
    @Bean
    public AuthInterceptor authInterceptor(ObjectMapper objectMapper) {
        return new AuthInterceptor(loginTokenMapper, objectMapper);
    }

    /**
     * 创建RoleIncepter Bean
     */
    @Bean
    public RoleInterceptor roleInterceptor(ObjectMapper objectMapper) {
        return new RoleInterceptor(objectMapper);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authInterceptor(objectMapper()))
                .addPathPatterns("/api/**", "/admin/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/files/**",
                        "/error",
                        "/favicon.ico",
                        "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg"
                );

        registry.addInterceptor(roleInterceptor(objectMapper()))
                .addPathPatterns("/api/**", "/admin/**")
                .excludePathPatterns( // ✅ 关键：Role 也要排除登录/登出
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/files/**",
                        "/error",
                        "/favicon.ico",
                        "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg"
                );
    }
}