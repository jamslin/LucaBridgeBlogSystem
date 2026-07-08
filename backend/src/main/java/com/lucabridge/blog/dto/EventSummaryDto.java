package com.lucabridge.blog.dto;

import java.time.Instant;

public record EventSummaryDto(
        Long id,
        String slug,
        String title,
        String summary,
        Instant startsAt,
        Instant endsAt,
        String locationText,
        String coverImageUrl,
        boolean fallback
) {}
