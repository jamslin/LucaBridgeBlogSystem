package com.lucabridge.blog.dto;

public record MediaDto(
        Long id,
        String url,
        Integer width,
        Integer height,
        String caption,
        Integer sortOrder
) {}
