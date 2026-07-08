package com.lucabridge.blog.dto;

public record PageSummaryDto(
        Long id,
        String slug,
        String pageType,
        String title,
        boolean fallback
) {}
