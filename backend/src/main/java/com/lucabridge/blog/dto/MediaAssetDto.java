package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

public record MediaAssetDto(
        Long id,
        String url,
        String filename,
        String contentType,
        Long sizeBytes,
        String altText,
        Instant createdAt,
        boolean inUse,
        List<MediaUsageDto> usages
) {}
