package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.StudentDayCourseDTO;
import com.example.leavesystem.mapper.TimetableMapper;
import com.example.leavesystem.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements TimetableService {

    private final TimetableMapper timetableMapper;

    @Override
    public List<StudentDayCourseDTO> getStudentDayTimetable(Long studentId, LocalDate date) {
        if (studentId == null || date == null) {
            throw new IllegalArgumentException("studentId 和 date 不能为空");
        }
        return timetableMapper.findDayCourses(studentId, date);
    }
}
