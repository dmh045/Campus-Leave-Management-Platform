package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 辅导员批量发起公假
 */
@Data
public class PublicLeaveBatchRequest {

    // 发起人（辅导员）
    private Long counselorId;

    // 公假学生列表
    private List<Long> studentIds;

    // 学期
    private Long termId;

    // 公假基本信息
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 公假影响的课程节次（对所有学生相同）
    private List<ImpactItem> impacts;

    @Data
    public static class ImpactItem {
        private Long offeringId;
        private LocalDate courseDate;
        private Integer sectionStart;
        private Integer sectionEnd;
    }
}
