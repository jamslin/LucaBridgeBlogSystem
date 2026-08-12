package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.AdminPostDetailDto;
import com.lucabridge.blog.dto.AdminPostSummaryDto;
import com.lucabridge.blog.dto.PostUpsertRequest;
import com.lucabridge.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin write + edit path for posts. Protected by SecurityConfig
 * (/api/admin/** requires ADMIN or EDITOR).
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<AdminPostSummaryDto> list() {
        return postService.listAllForAdmin();
    }

    @GetMapping("/{id}")
    public AdminPostDetailDto get(@PathVariable Long id) {
        return postService.getForEdit(id);
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

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublish(@PathVariable Long id) {
        postService.unpublish(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
