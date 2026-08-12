package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.AdminPageDetailDto;
import com.lucabridge.blog.dto.AdminPageSummaryDto;
import com.lucabridge.blog.dto.PageUpsertRequest;
import com.lucabridge.blog.service.AdminPageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pages")
public class AdminPageController {

    private final AdminPageService service;

    public AdminPageController(AdminPageService service) { this.service = service; }

    @GetMapping
    public List<AdminPageSummaryDto> list() { return service.list(); }

    @GetMapping("/{id}")
    public AdminPageDetailDto get(@PathVariable Long id) { return service.getForEdit(id); }

    @PostMapping
    public ResponseEntity<Long> upsert(@Valid @RequestBody PageUpsertRequest req) { return ResponseEntity.ok(service.upsert(req)); }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id) { service.setStatus(id, "PUBLISHED"); return ResponseEntity.ok().build(); }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublish(@PathVariable Long id) { service.setStatus(id, "DRAFT"); return ResponseEntity.ok().build(); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
