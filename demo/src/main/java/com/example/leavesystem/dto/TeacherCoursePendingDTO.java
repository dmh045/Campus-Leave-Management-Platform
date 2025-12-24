package com.example.leavesystem.dto;

import lombok.Data;

/**
 * 任课教师端：按课程维度聚合的待确认请假信息
 */
@Data
public class TeacherCoursePendingDTO {

    private Long offeringId;

    private String courseName;
    private String className;

    private Integer weekDay;       // 1-7
    private Integer sectionStart;
    private Integer sectionEnd;
    private String classroom;

    private Long pendingCount;     // 该课程下待确认的节次/记录数
}
