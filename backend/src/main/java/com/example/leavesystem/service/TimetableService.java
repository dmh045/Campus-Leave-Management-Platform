package com.example.leavesystem.service;

import com.example.leavesystem.dto.StudentDayCourseDTO;
import com.example.leavesystem.dto.TeacherDayCourseDTO;
import java.time.LocalDate;
import java.util.List;

public interface TimetableService {

    /**
     * 学生某一天的课表
     */
    List<StudentDayCourseDTO> getStudentDayTimetable(Long studentId, LocalDate date);
    List<TeacherDayCourseDTO> getTeacherDayTimetable(Long teacherId, LocalDate date);
}
