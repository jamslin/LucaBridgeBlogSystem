package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

/** Full editable post (raw translations, no language fallback) for the editor form. */
public record AdminPostDetailDto(
        Long id,
        String slug,
        String categoryKey,
        String coverImageUrl,
        Integer readingMinutes,
        String status,
        Instant publishedAt,
        List<PostTranslationInput> translations
) {}
