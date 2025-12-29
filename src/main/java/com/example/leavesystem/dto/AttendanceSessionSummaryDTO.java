package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老师查看某段时间内的签到场次列表
 */
@Data
public class AttendanceSessionSummaryDTO {

    private Long sessionId;

    private Long offeringId;

    private String courseName;

    private String className;

    private LocalDate courseDate;

    private Integer sectionStart;

    private Integer sectionEnd;

    private String status;  // OPEN / CLOSED

    private LocalDateTime allowStartTime;

    private LocalDateTime allowEndTime;

    private Integer shouldAttendCount;

    private Integer checkedInCount;

    private Integer leaveCount;
}
