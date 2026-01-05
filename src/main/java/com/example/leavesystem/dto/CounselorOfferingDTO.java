package com.example.leavesystem.dto;

import lombok.Data;

@Data
public class CounselorOfferingDTO {
    private Long offeringId;

    private Long courseId;
    private String courseName;

    private Long teacherId;
    private String teacherName;

    private Long classId;
    private String className;

    private Integer weekDay;        // 1-7
    private Integer sectionStart;
    private Integer sectionEnd;

    private String classroom;
}
