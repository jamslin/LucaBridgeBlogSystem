package com.lucabridge.core.blog.dto;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;
import java.util.List;

/** Every language at once — this is the editor's payload, not a rendered page. */
public record AdminBlogDetailDto(
        Long id,
        String slug,
        Long serviceId,
        Long coverMediaId,
        Long authorId,
        Integer readMinutes,
        GalleryLayout galleryLayout,
        PublishStatus status,
        Visibility.State state,
        Instant publishAt,
        Instant unpublishAt,
        Instant publishedAt,
        String tcTitle,
        String enTitle,
        String scTitle,
        String tcSummary,
        String enSummary,
        String scSummary,
        String tcBody,
        String enBody,
        String scBody,
        List<Long> galleryMediaIds) {
}
