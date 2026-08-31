package com.lucabridge.core.event.dto;

import java.time.Instant;

/** Public card view — one language, already resolved via Localized.pick(). Carries the same registration fields as the detail view; the home timeline needs "尚餘 N 個名額" on every row. */
public record EventSummaryDto(
        Long id,
        String slug,
        Long serviceId,
        String title,
        String summary,
        String venue,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant startsAt,
        Instant endsAt,
        RegistrationInfoDto registration) {
}
