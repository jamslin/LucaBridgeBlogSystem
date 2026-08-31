package com.lucabridge.core.job.dto;

import com.lucabridge.core.publish.PublishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record JobUpsertRequest(
        @NotBlank @Size(max = 200) String slug,
        @Size(max = 30) String employmentType,
        @Size(max = 120) String department,
        Instant postedAt,
        Instant closesAt,
        @Size(max = 320) String applyEmail,
        @Size(max = 1000) String applyUrl,
        @NotNull PublishStatus status,
        Instant publishAt,
        Instant unpublishAt,
        @NotBlank @Size(max = 300) String tcTitle,
        @Size(max = 300) String enTitle,
        @Size(max = 300) String scTitle,
        String tcBody,
        String enBody,
        String scBody,
        @Size(max = 300) String tcLocation,
        @Size(max = 300) String enLocation,
        @Size(max = 300) String scLocation) {
}
