package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.service.HomepageBannerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin/banners")
public class AdminHomepageBannerController {
    private final HomepageBannerService service;
    public AdminHomepageBannerController(HomepageBannerService service) { this.service = service; }
    @GetMapping public List<AdminHomepageBannerDto> list() { return service.adminList(); }
    @PostMapping public Long save(@Valid @RequestBody HomepageBannerUpsertRequest request) { return service.upsert(request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
