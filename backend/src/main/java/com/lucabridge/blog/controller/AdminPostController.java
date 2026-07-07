package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.PostUpsertRequest;
import com.lucabridge.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin write path — TODO per Coding Plan Phase 4. Endpoints are wired to PostService
 * and protected by SecurityConfig (/api/admin/** requires ROLE_ADMIN), but have not yet
 * been exercised by the admin SPA or integration tests.
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Long> upsert(@Valid @RequestBody PostUpsertRequest request) {
        return ResponseEntity.ok(postService.upsertDraft(request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id) {
        postService.publish(id);
        return ResponseEntity.ok().build();
    }
}
