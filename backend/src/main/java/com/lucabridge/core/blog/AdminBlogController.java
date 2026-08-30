package com.lucabridge.core.blog;

import com.lucabridge.core.blog.dto.AdminBlogDetailDto;
import com.lucabridge.core.blog.dto.AdminBlogSummaryDto;
import com.lucabridge.core.blog.dto.BlogUpsertRequest;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Every route here is under /api/admin/**, which SecurityConfig already restricts to
 * ADMIN/EDITOR (DELETE further narrowed to ADMIN-only) — no per-method role check needed.
 */
@RestController
@RequestMapping("/api/admin/blog")
public class AdminBlogController {

    private final BlogService blogService;
    private final CurrentUser currentUser;

    public AdminBlogController(BlogService blogService, CurrentUser currentUser) {
        this.blogService = blogService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public Page<AdminBlogSummaryDto> list(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Instant now = Instant.now();
        return blogService.listActive(pageable).map(blog -> BlogMapper.toAdminSummary(blog, now));
    }

    @GetMapping("/{id}")
    public AdminBlogDetailDto get(@PathVariable Long id) {
        return BlogMapper.toAdminDetail(blogService.getActiveById(id), Instant.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminBlogDetailDto create(@Valid @RequestBody BlogUpsertRequest request) {
        Blog blog = blogService.create(request, currentUser.id());
        return BlogMapper.toAdminDetail(blog, Instant.now());
    }

    @PutMapping("/{id}")
    public AdminBlogDetailDto update(@PathVariable Long id, @Valid @RequestBody BlogUpsertRequest request) {
        Blog blog = blogService.update(id, request, currentUser.id());
        return BlogMapper.toAdminDetail(blog, Instant.now());
    }

    /** Soft delete only — see BlogService.softDelete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        blogService.softDelete(id, currentUser.id());
    }
}
