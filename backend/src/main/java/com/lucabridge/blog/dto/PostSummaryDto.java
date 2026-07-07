package com.lucabridge.blog.dto;

import java.time.Instant;

public record PostSummaryDto(
        Long id,
        String slug,
        String title,
        String excerpt,
        String coverImageUrl,
        Integer readingMinutes,
        Instant publishedAt,
        CategoryDto category,
        boolean fallback
) {}
