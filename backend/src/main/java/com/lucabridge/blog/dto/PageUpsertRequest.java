package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PageUpsertRequest(
        Long id,
        @NotBlank String slug,
        String pageType,
        Integer sortOrder,
        String heroImageUrl,
        @NotEmpty List<PageTranslationInput> translations
) {}
