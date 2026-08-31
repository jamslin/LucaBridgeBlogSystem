package com.lucabridge.core.job.dto;

import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;

public record AdminJobDetailDto(
        Long id,
        String slug,
        String employmentType,
        String department,
        Instant postedAt,
        Instant closesAt,
        String applyEmail,
        String applyUrl,
        PublishStatus status,
        Visibility.State state,
        Instant publishAt,
        Instant unpublishAt,
        Instant publishedAt,
        String tcTitle,
        String enTitle,
        String scTitle,
        String tcBody,
        String enBody,
        String scBody,
        String tcLocation,
        String enLocation,
        String scLocation) {
}
