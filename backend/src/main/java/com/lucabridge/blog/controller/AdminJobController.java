package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.AdminJobDetailDto;
import com.lucabridge.blog.dto.AdminJobSummaryDto;
import com.lucabridge.blog.dto.JobUpsertRequest;
import com.lucabridge.blog.service.AdminJobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jobs")
public class AdminJobController {

    private final AdminJobService service;

    public AdminJobController(AdminJobService service) { this.service = service; }

    @GetMapping
    public List<AdminJobSummaryDto> list() { return service.list(); }

    @GetMapping("/{id}")
    public AdminJobDetailDto get(@PathVariable Long id) { return service.getForEdit(id); }

    @PostMapping
    public ResponseEntity<Long> upsert(@Valid @RequestBody JobUpsertRequest req) { return ResponseEntity.ok(service.upsert(req)); }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id) { service.setStatus(id, "PUBLISHED"); return ResponseEntity.ok().build(); }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublish(@PathVariable Long id) { service.setStatus(id, "DRAFT"); return ResponseEntity.ok().build(); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
