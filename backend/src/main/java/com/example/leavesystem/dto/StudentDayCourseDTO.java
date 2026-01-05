package com.example.leavesystem.dto;

import lombok.Data;

@Data
public class StudentDayCourseDTO {

    private Long offeringId;
    private Long courseId;
    private String courseName;
    private String teacherName;
    private Integer weekDay;        // 1-7
    private Integer sectionStart;
    private Integer sectionEnd;
    private String classroom;

    /**
     * 课表状态（与前端 LeaveStatus 对齐）：
     * 200=到课, 5=已请假(已批准), 4=待确认/待审核, 100=公假, 3/6/7=驳回/取消/结束
     */
    private Integer status;
}
