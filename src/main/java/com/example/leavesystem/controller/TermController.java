package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Term;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @PostMapping
    @RequiresRoles
    public Result create(@RequestBody Term term) {
        Term created = termService.create(term);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @RequiresRoles
    public Result update(@PathVariable Long id, @RequestBody Term term) {
        term.setTermId(id);
        Term updated = termService.update(term);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresRoles
    public Result delete(@PathVariable Long id) {
        boolean ok = termService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    @RequiresRoles
    public Result get(@PathVariable Long id) {
        Term term = termService.findById(id);
        return term != null ? Result.success(term) : Result.failure("未找到学期");
    }

    @GetMapping
    @RequiresRoles
    public Result list() {
        List<Term> list = termService.listAll();
        return Result.success(list);
    }

    @PostMapping("/{id}/open")
    @RequiresRoles
    public Result open(@PathVariable Long id) {
        Term term = termService.openTerm(id);
        return term != null ? Result.success(term) : Result.failure("打开学期失败");
    }

    @PostMapping("/{id}/close")
    @RequiresRoles
    public Result close(@PathVariable Long id) {
        Term term = termService.closeTerm(id);
        return term != null ? Result.success(term) : Result.failure("关闭学期失败");
    }
}