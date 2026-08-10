package com.lucabridge.blog.dto;

import java.time.Instant;

public record JobSummaryDto(
        Long id,
        String slug,
        String title,
        String summary,
        String department,
        String employmentType,
        String employmentTypeLabel,
        String locationText,
        Instant postedAt,
        Instant closesAt,
        boolean fallback
) {}
