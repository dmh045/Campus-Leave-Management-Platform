package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Clazz;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/classes")
@RequiredArgsConstructor
@RequiresRoles("ADMIN") // MOD: 只在类上限制 ADMIN，方法上不要再写空 @RequiresRoles（会覆盖）
public class ClassController {

    private final ClassService classService;

    @PostMapping
    public Result create(@RequestBody Clazz clazz) {
        Clazz created = classService.create(clazz);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Clazz clazz) {
        clazz.setClassId(id);
        Clazz updated = classService.update(clazz);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean ok = classService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        Clazz clazz = classService.findById(id);
        return clazz != null ? Result.success(clazz) : Result.failure("未找到班级");
    }

    @GetMapping
    public Result list() {
        List<Clazz> list = classService.listAll();
        return Result.success(list);
    }

    @PutMapping("/{id}/counselor")
    public Result setCounselor(@PathVariable Long id, @RequestBody CounselorRequest request) {
        Clazz clazz = classService.setCounselor(id, request.getCounselorId());
        return clazz != null ? Result.success(clazz) : Result.failure("设置辅导员失败");
    }

    private static class CounselorRequest {
        private Long counselorId;

        public Long getCounselorId() {
            return counselorId;
        }

        public void setCounselorId(Long counselorId) {
            this.counselorId = counselorId;
        }
    }
}
