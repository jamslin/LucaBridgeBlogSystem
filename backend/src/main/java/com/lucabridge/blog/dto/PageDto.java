package com.lucabridge.blog.dto;

public record PageDto(
        Long id,
        String slug,
        String pageType,
        String title,
        String subtitle,
        String bodyMarkdown,
        String heroImageUrl,
        boolean fallback
) {}
