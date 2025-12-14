package com.example.leavesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老师发起签到场次后返回的信息
 */
@Data
@AllArgsConstructor
public class AttendanceSessionStartResponse {

    private Long sessionId;

    /**
     * 本次签到的token，前端可以编码到二维码中
     */
    private String token;

    private LocalDateTime allowStartTime;

    private LocalDateTime allowEndTime;

    private LocalDateTime tokenExpireTime;
}
