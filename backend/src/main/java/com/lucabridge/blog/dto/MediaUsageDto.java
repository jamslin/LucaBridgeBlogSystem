package com.lucabridge.blog.dto;

/** Where a media asset is referenced. field = "cover" or "body". */
public record MediaUsageDto(
        Long postId,
        String slug,
        String title,
        String field
) {}
