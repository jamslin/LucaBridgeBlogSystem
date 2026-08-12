package com.lucabridge.blog.dto;

import java.time.Instant;

/** Row in the admin posts table — shows all statuses, not just published. */
public record AdminPostSummaryDto(
        Long id,
        String slug,
        String status,
        String categoryKey,
        String title,
        Instant publishedAt
) {}
