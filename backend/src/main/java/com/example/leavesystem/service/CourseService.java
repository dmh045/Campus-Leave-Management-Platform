package com.example.leavesystem.service;

import com.example.leavesystem.entity.Course;
import java.util.List;

public interface CourseService {
    Course create(Course course);
    Course update(Course course);
    boolean deleteById(Long id);
    Course findById(Long id);
    List<Course> listAll();
    Course findByCode(String code);
}