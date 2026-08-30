package com.lucabridge.core.blog.dto;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.publish.PublishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record BlogUpsertRequest(
        @NotBlank @Size(max = 200) String slug,
        Long serviceId,
        Long coverMediaId,
        Long authorId,
        Integer readMinutes,
        GalleryLayout galleryLayout,
        @NotNull PublishStatus status,
        Instant publishAt,
        Instant unpublishAt,
        @NotBlank @Size(max = 300) String tcTitle,
        @Size(max = 300) String enTitle,
        @Size(max = 300) String scTitle,
        @Size(max = 600) String tcSummary,
        @Size(max = 600) String enSummary,
        @Size(max = 600) String scSummary,
        String tcBody,
        String enBody,
        String scBody,
        List<Long> galleryMediaIds) {
}
