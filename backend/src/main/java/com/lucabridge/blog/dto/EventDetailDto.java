package com.lucabridge.blog.dto;

import java.time.Instant;

public record EventDetailDto(
        Long id,
        String slug,
        String title,
        String summary,
        String bodyMarkdown,
        Instant startsAt,
        Instant endsAt,
        String locationText,
        String coverImageUrl,
        boolean fallback
) {}
