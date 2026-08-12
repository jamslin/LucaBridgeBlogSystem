package com.lucabridge.blog.dto;

import java.util.List;

public record AdminPageDetailDto(
        Long id,
        String slug,
        String status,
        String pageType,
        Integer sortOrder,
        String heroImageUrl,
        List<PageTranslationInput> translations
) {}
