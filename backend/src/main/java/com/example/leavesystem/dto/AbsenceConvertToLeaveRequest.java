package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 将缺勤记录转成一张新的请假单的请求
 */
@Data
public class AbsenceConvertToLeaveRequest {

    private Long studentId;      // 谁在发起（一般就是这条缺勤对应的 student_id）
    private Long termId;         // 当前学期ID

    private String leaveType;    // SICK / AFFAIR / PUBLIC
    private String reason;       // 补假理由
    private String proofUrl;     // 补充材料，可选

    /**
     * 事后补假希望覆盖的时间段，可以直接沿用原请假 apply 的逻辑
     * （如果你不关心具体时间，也可以前端传 courseDate 当天的 08:00~18:00）
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
