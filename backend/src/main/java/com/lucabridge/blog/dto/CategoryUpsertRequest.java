package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CategoryUpsertRequest(
        Long id,
        @NotBlank String key,
        Integer sortOrder,
        @NotEmpty List<CategoryTranslationInput> translations
) {}
