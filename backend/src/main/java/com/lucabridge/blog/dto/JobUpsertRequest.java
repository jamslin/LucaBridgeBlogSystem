package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public record JobUpsertRequest(
        Long id,
        @NotBlank String slug,
        String employmentType,
        String department,
        String locationText,
        Instant postedAt,
        Instant closesAt,
        @NotEmpty List<JobTranslationInput> translations
) {}
