package com.example.leavesystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 某一次签到场次的详细情况
 */
@Data
public class AttendanceSessionDetailDTO {

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

    private Integer absenceCount;

    private List<StudentStatusItem> students;

    @Data
    public static class StudentStatusItem {
        private Long studentId;
        private String studentNo;
        private String studentName;
        /**
         * PRESENT / ABSENT / LEAVE
         */
        private String status;
        private LocalDateTime checkinTime;
        /**
         * 如果是请假，关联的 leaveId（可选，看你是否需要）
         */
        private Long leaveId;
    }
}
