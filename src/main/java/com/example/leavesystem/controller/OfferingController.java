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
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    @RequiresRoles
    public Result create(@RequestBody Offering offering) {
        Offering created = offeringService.create(offering);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @RequiresRoles
    public Result update(@PathVariable Long id, @RequestBody Offering offering) {
        offering.setOfferingId(id);
        Offering updated = offeringService.update(offering);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresRoles
    public Result delete(@PathVariable Long id) {
        boolean ok = offeringService.deleteById(id);
        return ok ? Result.success(null) : Result.failure("删除失败");
    }

    @GetMapping("/{id}")
    @RequiresRoles
    public Result get(@PathVariable Long id) {
        Offering offering = offeringService.findById(id);
        return offering != null ? Result.success(offering) : Result.failure("未找到开课记录");
    }

    @GetMapping
    @RequiresRoles
    public Result list() {
        List<Offering> list = offeringService.listAll();
        return Result.success(list);
    }

    @GetMapping("/by-term-class")
    @RequiresRoles
    public Result getByTermAndClass(@RequestParam Long termId, @RequestParam Long classId) {
        List<Offering> offerings = offeringService.findByTermAndClass(termId, classId);
        return Result.success(offerings);
    }
}