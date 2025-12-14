package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 老师发起一次签到场次的请求
 */
@Data
public class AttendanceSessionStartRequest {

    /**
     * 任课老师ID（Staff.staffId）
     */
    private Long teacherId;

    /**
     * 开课ID（Offering.offeringId）
     */
    private Long offeringId;

    /**
     * 上课日期
     */
    private LocalDate courseDate;

    /**
     * 节次范围
     */
    private Integer sectionStart;

    private Integer sectionEnd;

    /**
     * 签到允许时长（分钟），例如 10 分钟
     */
    private Integer durationMinutes;
}
