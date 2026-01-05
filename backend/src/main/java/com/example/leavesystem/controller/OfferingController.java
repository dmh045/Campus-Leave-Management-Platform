package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Offering;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/offerings")
@RequiredArgsConstructor
@RequiresRoles("ADMIN") // MOD: 后台基础数据接口统一仅 ADMIN 可访问
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    public Result create(@RequestBody Offering offering) {
        Offering created = offeringService.create(offering);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Offering offering) {
        offering.setOfferingId(id);
        Offering updated = offeringService.update(offering);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean ok = offeringService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        Offering offering = offeringService.findById(id);
        return offering != null ? Result.success(offering) : Result.failure("未找到开课记录");
    }

    @GetMapping
    public Result list() {
        List<Offering> list = offeringService.listAll();
        return Result.success(list);
    }

    @GetMapping("/by-term-class")
    public Result getByTermAndClass(@RequestParam Long termId, @RequestParam Long classId) {
        List<Offering> offerings = offeringService.findByTermAndClass(termId, classId);
        return Result.success(offerings);
    }
}
