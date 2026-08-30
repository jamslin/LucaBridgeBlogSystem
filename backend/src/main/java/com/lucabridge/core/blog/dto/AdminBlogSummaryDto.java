package com.lucabridge.core.blog.dto;

import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;

/** {@code state} is what the CMS badge shows — never re-derive "live or not" from status alone in the frontend. */
public record AdminBlogSummaryDto(
        Long id,
        String slug,
        String tcTitle,
        PublishStatus status,
        Visibility.State state,
        Instant publishAt,
        Instant unpublishAt,
        Instant publishedAt,
        Instant updatedAt) {
}
