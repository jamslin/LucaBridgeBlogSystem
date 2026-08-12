package com.lucabridge.blog.dto;

import java.util.List;

public record AdminCategoryDto(
        Long id,
        String key,
        Integer sortOrder,
        List<CategoryTranslationInput> translations,
        long postCount
) {}
