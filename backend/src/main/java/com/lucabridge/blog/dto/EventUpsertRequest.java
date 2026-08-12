package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public record EventUpsertRequest(
        Long id,
        @NotBlank String slug,
        Instant startsAt,
        Instant endsAt,
        String locationText,
        String coverImageUrl,
        @NotEmpty List<EventTranslationInput> translations
) {}
