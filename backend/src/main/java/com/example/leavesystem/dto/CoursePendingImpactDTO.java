package com.example.leavesystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoursePendingImpactDTO {
    private Long courseId;
    private String courseName;
    private List<TeacherPendingImpactDTO> impacts;
}