package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

public record AdminEventDetailDto(
        Long id,
        String slug,
        String status,
        Instant startsAt,
        Instant endsAt,
        String locationText,
        String coverImageUrl,
        List<EventTranslationInput> translations
) {}
