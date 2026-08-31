package com.lucabridge.core.job.dto;

import java.time.Instant;

public record JobDetailDto(
        Long id,
        String slug,
        String title,
        String employmentType,
        String department,
        String location,
        String body,
        String applyEmail,
        String applyUrl,
        Instant closesAt,
        Instant publishedAt) {
}
