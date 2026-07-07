package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

public record PostDetailDto(
        Long id,
        String slug,
        String title,
        String subtitle,
        String bodyMarkdown,
        String coverImageUrl,
        Integer readingMinutes,
        Instant publishedAt,
        CategoryDto category,
        List<MediaDto> media,
        List<PressLinkDto> pressLinks,
        PostSummaryDto previous,
        PostSummaryDto next,
        boolean fallback
) {}
