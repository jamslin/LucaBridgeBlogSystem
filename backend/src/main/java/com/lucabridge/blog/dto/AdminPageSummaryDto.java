package com.lucabridge.blog.dto;

public record AdminPageSummaryDto(
        Long id,
        String slug,
        String status,
        String pageType,
        Integer sortOrder,
        String title
) {}
