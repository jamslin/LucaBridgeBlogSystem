package com.lucabridge.core.job.dto;

import java.time.Instant;

/** Public card view — one language, already resolved via Localized.pick(). */
public record JobSummaryDto(
        Long id,
        String slug,
        String title,
        String employmentType,
        String department,
        String location,
        Instant closesAt,
        Instant publishedAt) {
}
