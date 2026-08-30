package com.lucabridge.core.blog.dto;

import com.lucabridge.core.content.GalleryLayout;

import java.time.Instant;
import java.util.List;

public record BlogDetailDto(
        Long id,
        String slug,
        String title,
        String summary,
        String body,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant publishedAt,
        Integer readMinutes,
        GalleryLayout galleryLayout,
        List<GalleryImageDto> gallery) {
}
