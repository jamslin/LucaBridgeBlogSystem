package com.lucabridge.core.homeblock.dto;

import com.lucabridge.core.homeblock.HomeBlockSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record HomeBlockUpsertRequest(
        @NotNull HomeBlockSlot slot,
        Long mediaId,
        Long blogId,
        @Size(max = 1000) String linkUrl,
        Integer sortOrder,
        boolean active,
        Instant publishAt,
        Instant unpublishAt,
        @Size(max = 120) String tcEyebrow,
        @Size(max = 120) String enEyebrow,
        @Size(max = 120) String scEyebrow,
        @NotBlank @Size(max = 300) String tcTitle,
        @Size(max = 300) String enTitle,
        @Size(max = 300) String scTitle,
        @Size(max = 600) String tcSubtitle,
        @Size(max = 600) String enSubtitle,
        @Size(max = 600) String scSubtitle,
        @Size(max = 100) String tcButtonLabel,
        @Size(max = 100) String enButtonLabel,
        @Size(max = 100) String scButtonLabel,
        @Size(max = 300) String tcNote,
        @Size(max = 300) String enNote,
        @Size(max = 300) String scNote) {
}
