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
@RequiresRoles("ADMIN") // MOD: 明确只有 ADMIN 能访问（放在类上即可，方法上就不用重复）
public class TermController {

    private final TermService termService;

    @PostMapping
    public Result<Term> create(@RequestBody Term term) { // MOD: 补泛型
        Term created = termService.create(term);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Term> update(@PathVariable Long id, @RequestBody Term term) { // MOD: 补泛型
        term.setTermId(id);
        Term updated = termService.update(term);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { // MOD: 补泛型
        boolean ok = termService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    public Result<Term> get(@PathVariable Long id) { // MOD: 补泛型
        Term term = termService.findById(id);
        // MOD: 建议用 error(404,...) 更清晰（你如果暂时不想改语义，也可继续 failure）
        return term != null ? Result.success(term) : Result.error(404, "未找到学期");
    }

    @GetMapping
    public Result<List<Term>> list() { // MOD: 补泛型
        List<Term> list = termService.listAll();
        return Result.success(list);
    }

    @PostMapping("/{id}/open")
    public Result<Term> open(@PathVariable Long id) { // MOD: 补泛型
        Term term = termService.openTerm(id);
        return term != null ? Result.success(term) : Result.failure("打开学期失败");
    }

    @PostMapping("/{id}/close")
    public Result<Term> close(@PathVariable Long id) { // MOD: 补泛型
        Term term = termService.closeTerm(id);
        return term != null ? Result.success(term) : Result.failure("关闭学期失败");
    }
}
