package com.lucabridge.core.event.dto;

import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;

public record AdminEventSummaryDto(
        Long id,
        String slug,
        Long serviceId,
        String tcTitle,
        PublishStatus status,
        Visibility.State state,
        Instant startsAt,
        Instant updatedAt,
        RegistrationInfoDto registration) {
}
