package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Course;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @RequiresRoles
    public Result create(@RequestBody Course course) {
        Course created = courseService.create(course);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @RequiresRoles
    public Result update(@PathVariable Long id, @RequestBody Course course) {
        course.setCourseId(id);
        Course updated = courseService.update(course);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresRoles
    public Result delete(@PathVariable Long id) {
        boolean ok = courseService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    @RequiresRoles
    public Result get(@PathVariable Long id) {
        Course course = courseService.findById(id);
        return course != null ? Result.success(course) : Result.failure("未找到课程");
    }

    @GetMapping
    @RequiresRoles
    public Result list() {
        List<Course> list = courseService.listAll();
        return Result.success(list);
    }

    @GetMapping("/by-code/{code}")
    @RequiresRoles
    public Result getByCode(@PathVariable String code) {
        Course course = courseService.findByCode(code);
        return course != null ? Result.success(course) : Result.failure("未找到课程");
    }
}