package com.lucabridge.core.media.dto;

import java.time.Instant;

public record AdminMediaDto(
        Long id,
        String url,
        String fileName,
        String mimeType,
        Integer width,
        Integer height,
        Long byteSize,
        String altText,
        boolean inUse,
        long usageCount,
        Instant createdAt) {
}
