package com.example.leavesystem.dto;

import lombok.Data;

/**
 * 学生扫码 / 输入口令进行签到
 */
@Data
public class StudentCheckinRequest {

    /**
     * 学生ID（Student.studentId）
     */
    private Long studentId;

    /**
     * 签到token（来自二维码）
     */
    private String token;
}
