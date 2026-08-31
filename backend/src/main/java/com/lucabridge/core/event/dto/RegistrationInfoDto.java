package com.lucabridge.core.event.dto;

import com.lucabridge.core.event.RegistrationState;

/**
 * The backend-computed registration contract (design 7c · 五個報名狀態) — the frontend must
 * never derive {@code state} itself. {@code almostFull} is true when remaining <= 20% of
 * capacity, for the "即將額滿" badge; false whenever capacity is null (unlimited).
 */
public record RegistrationInfoDto(
        RegistrationState state,
        Integer capacity,
        long registeredCount,
        Integer remaining,
        boolean almostFull) {
}
