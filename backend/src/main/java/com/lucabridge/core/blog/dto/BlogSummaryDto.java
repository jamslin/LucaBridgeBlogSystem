package com.lucabridge.core.blog.dto;

import java.time.Instant;

/** Public card view — one language, already resolved via Localized.pick(). */
public record BlogSummaryDto(
        Long id,
        String slug,
        /** Localised name of the service this post belongs to — the card's tag. */
        String serviceName,
        String title,
        String summary,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant publishedAt,
        Integer readMinutes) {
}
