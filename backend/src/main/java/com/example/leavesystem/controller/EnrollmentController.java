package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Enrollment;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
@RequiresRoles("ADMIN") // MOD: 后台基础数据接口统一仅 ADMIN 可访问
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public Result create(@RequestBody Enrollment enrollment) {
        Enrollment created = enrollmentService.create(enrollment);
        return Result.success(created);
    }

    @PostMapping("/batch")
    public Result batchCreate(@RequestBody List<Enrollment> enrollments) {
        List<Enrollment> created = enrollmentService.batchCreate(enrollments);
        return Result.success(created);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        boolean ok = enrollmentService.updateStatus(id, request.getStatus());
        return ok ? Result.success(null) : Result.failure("更新状态失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean ok = enrollmentService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        Enrollment enrollment = enrollmentService.findById(id);
        return enrollment != null ? Result.success(enrollment) : Result.failure("未找到选课记录");
    }

    @GetMapping("/by-student/{studentId}")
    public Result getByStudentId(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        return Result.success(enrollments);
    }

    @GetMapping("/by-offering/{offeringId}")
    public Result getByOfferingId(@PathVariable Long offeringId) {
        List<Enrollment> enrollments = enrollmentService.findByOfferingId(offeringId);
        return Result.success(enrollments);
    }

    // 内部静态类，用于接收更新状态的请求参数
    private static class StatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
