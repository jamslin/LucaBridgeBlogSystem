package com.lucabridge.core.job.dto;

import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;

/** state uses Visibility.stateOfJob, not stateOf — an admin must never see LIVE for a posting closesAt has already dropped from the public list. */
public record AdminJobSummaryDto(
        Long id,
        String slug,
        String tcTitle,
        PublishStatus status,
        Visibility.State state,
        Instant closesAt,
        Instant updatedAt) {
}
