package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.AdminCategoryDto;
import com.lucabridge.blog.dto.CategoryUpsertRequest;
import com.lucabridge.blog.service.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService service;

    public AdminCategoryController(AdminCategoryService service) { this.service = service; }

    @GetMapping
    public List<AdminCategoryDto> list() { return service.list(); }

    @PostMapping
    public ResponseEntity<Long> upsert(@Valid @RequestBody CategoryUpsertRequest req) { return ResponseEntity.ok(service.upsert(req)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
