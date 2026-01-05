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
@RequiresRoles("ADMIN") // 后台基础数据接口仅管理员可操作
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public Result<Course> create(@RequestBody Course course) { // MOD: 补泛型 + 去掉方法级空注解
        Course created = courseService.create(course);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course course) { // MOD
        course.setCourseId(id);
        Course updated = courseService.update(course);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { // MOD
        boolean ok = courseService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    public Result<Course> get(@PathVariable Long id) { // MOD
        Course course = courseService.findById(id);
        return course != null ? Result.success(course) : Result.error(404, "未找到课程"); // MOD: 404 更语义化
    }

    @GetMapping
    public Result<List<Course>> list() { // MOD
        List<Course> list = courseService.listAll();
        return Result.success(list);
    }

    @GetMapping("/by-code/{code}")
    public Result<Course> getByCode(@PathVariable String code) { // MOD
        Course course = courseService.findByCode(code);
        return course != null ? Result.success(course) : Result.error(404, "未找到课程");
    }
}
