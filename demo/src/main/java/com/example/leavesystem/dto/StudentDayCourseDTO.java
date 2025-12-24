package com.example.leavesystem.dto;

import lombok.Data;

@Data
public class StudentDayCourseDTO {

    private Long offeringId;
    private Long courseId;
    private String courseName;
    private Integer weekDay;        // 1-7
    private Integer sectionStart;
    private Integer sectionEnd;
    private String classroom;
}
