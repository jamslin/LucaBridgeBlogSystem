package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

public record AdminJobDetailDto(
        Long id,
        String slug,
        String status,
        String employmentType,
        String department,
        String locationText,
        Instant postedAt,
        Instant closesAt,
        List<JobTranslationInput> translations
) {}
