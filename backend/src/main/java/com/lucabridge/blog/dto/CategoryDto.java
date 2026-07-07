package com.lucabridge.blog.dto;

public record CategoryDto(
        Long id,
        String key,
        String name,
        boolean fallback
) {}
