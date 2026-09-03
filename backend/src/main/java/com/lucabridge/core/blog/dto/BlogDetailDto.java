package com.lucabridge.core.blog.dto;

import com.lucabridge.core.content.GalleryLayout;

import java.time.Instant;
import java.util.List;

public record BlogDetailDto(
        Long id,
        String slug,
        /** Localised name of the service this post belongs to — the article's tag. */
        String serviceName,
        String title,
        String summary,
        String body,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant publishedAt,
        Integer readMinutes,
        GalleryLayout galleryLayout,
        List<GalleryImageDto> gallery,
        /** Older post, by publish time. Null on the oldest. */
        BlogNeighbourDto prev,
        /** Newer post, by publish time. Null on the newest. */
        BlogNeighbourDto next) {
}
