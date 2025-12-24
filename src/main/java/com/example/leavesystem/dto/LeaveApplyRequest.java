package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveApplyRequest {

    private Long studentId;
    private Long termId;
    private String leaveType;      // SICK / PERSONAL / PUBLIC
    private String applyChannel;   // BY_COURSE / BY_TIME
    private String reason;
    private String proofUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 受影响的课程节次列表（按课表维度请假时需要）
    private List<ImpactItem> impacts;

    @Data
    public static class ImpactItem {
        private Long offeringId;
        private LocalDate courseDate;
        private Integer sectionStart;
        private Integer sectionEnd;
    }
}
