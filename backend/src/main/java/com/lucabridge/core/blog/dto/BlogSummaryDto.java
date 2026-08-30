package com.lucabridge.core.blog.dto;

import java.time.Instant;

/** Public card view — one language, already resolved via Localized.pick(). */
public record BlogSummaryDto(
        Long id,
        String slug,
        String title,
        String summary,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant publishedAt,
        Integer readMinutes) {
}
