package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Create/update payload for POST /api/admin/posts.
 * Note: at least the zh-Hant (繁中) translation is required before a post can be published —
 * enforced in PostService, not here, since drafts may be saved with partial translations.
 */
public record PostUpsertRequest(
        Long id,
        @NotBlank String slug,
        @NotBlank String categoryKey,
        String coverImageUrl,
        Integer readingMinutes,
        @NotEmpty List<PostTranslationInput> translations
) {}
