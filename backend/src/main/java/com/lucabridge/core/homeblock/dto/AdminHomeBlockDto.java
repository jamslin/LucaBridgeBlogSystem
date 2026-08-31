package com.lucabridge.core.homeblock.dto;

import com.lucabridge.core.homeblock.HomeBlockSlot;

import java.time.Instant;

public record AdminHomeBlockDto(
        Long id,
        HomeBlockSlot slot,
        Long mediaId,
        Long blogId,
        String linkUrl,
        Integer sortOrder,
        boolean active,
        Instant publishAt,
        Instant unpublishAt,
        Instant updatedAt,
        String tcTitle,
        String enTitle,
        String scTitle,
        String tcSubtitle,
        String enSubtitle,
        String scSubtitle,
        String tcButtonLabel,
        String enButtonLabel,
        String scButtonLabel) {
}
