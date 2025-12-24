package com.example.leavesystem.dto;

import lombok.Data;

/**
 * 班级请假统计（看板用）
 */
@Data
public class ClassLeaveStatsDTO {

    private Long classId;
    private String className;

    // 总请假单数
    private long totalLeaves;

    // 各类型请假数
    private long sickCount;     // 病假
    private long affairCount;   // 事假
    private long publicCount;   // 公假

    // 状态统计
    private long pendingCount;   // 待处理（辅导员/老师任一环节未结束）
    private long approvedCount;  // 审批通过
    private long rejectedCount;  // 被驳回或退回
}
