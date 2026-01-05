package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Offering {

    private Long offeringId;
    private Long termId;
    private Long courseId;
    private Long classId;
    private Long teacherId;
    private Integer weekDay;        // 1-7
    private Integer sectionStart;
    private Integer sectionEnd;
    private String classroom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
