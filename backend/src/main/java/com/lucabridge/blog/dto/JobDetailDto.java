package com.lucabridge.blog.dto;

import java.time.Instant;

public record JobDetailDto(
        Long id,
        String slug,
        String title,
        String summary,
        String bodyMarkdown,
        String department,
        String employmentType,
        String employmentTypeLabel,
        String locationText,
        Instant postedAt,
        Instant closesAt,
        boolean fallback
) {}
