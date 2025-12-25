package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Course;
import com.example.leavesystem.mapper.CourseMapper;
import com.example.leavesystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public Course create(Course course) {
        courseMapper.insert(course);
        return course;
    }

    @Override
    public Course update(Course course) {
        courseMapper.update(course);
        return course;
    }

    @Override
    public boolean deleteById(Long id) {
        return courseMapper.delete(id) > 0;
    }

    @Override
    public Course findById(Long id) {
        return courseMapper.findById(id);
    }

    @Override
    public List<Course> listAll() {
        return courseMapper.findAll();
    }

    @Override
    public Course findByCode(String code) {
        return courseMapper.findByCode(code);
    }
}