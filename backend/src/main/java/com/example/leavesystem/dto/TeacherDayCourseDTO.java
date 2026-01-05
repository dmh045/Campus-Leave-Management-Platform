package com.example.leavesystem.dto;

import lombok.Data;

@Data
public class TeacherDayCourseDTO {

    private Long offeringId;
    private Long courseId;
    private String courseName;

    private Long classId;
    private String className;

    private Integer weekDay;
    private Integer sectionStart;
    private Integer sectionEnd;

    private String classroom;
}
