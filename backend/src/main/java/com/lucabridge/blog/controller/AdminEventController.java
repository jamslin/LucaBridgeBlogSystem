package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.AdminEventDetailDto;
import com.lucabridge.blog.dto.AdminEventSummaryDto;
import com.lucabridge.blog.dto.EventUpsertRequest;
import com.lucabridge.blog.service.AdminEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final AdminEventService service;

    public AdminEventController(AdminEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminEventSummaryDto> list() { return service.list(); }

    @GetMapping("/{id}")
    public AdminEventDetailDto get(@PathVariable Long id) { return service.getForEdit(id); }

    @PostMapping
    public ResponseEntity<Long> upsert(@Valid @RequestBody EventUpsertRequest req) {
        return ResponseEntity.ok(service.upsert(req));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id) { service.publish(id); return ResponseEntity.ok().build(); }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublish(@PathVariable Long id) { service.unpublish(id); return ResponseEntity.ok().build(); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
