package com.lucabridge.core.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the five states from the design's component sheet (7c · 五個報名狀態) — this is a
 * backend contract, not styling, so it gets the same treatment CoreFoundationTest gives
 * Visibility.
 */
class RegistrationStateTest {

    private static final Instant NOW = Instant.parse("2026-08-30T12:00:00Z");
    private static final Instant PAST = NOW.minusSeconds(3600);
    private static final Instant FUTURE = NOW.plusSeconds(3600);

    @Test
    @DisplayName("STATE 5 · not registerable overrides everything else, dates included")
    void notRegisterableOverridesAllOtherSignals() {
        assertEquals(RegistrationState.NOT_REGISTERABLE,
                RegistrationState.of(false, PAST, FUTURE, 10, 0, NOW));
    }

    @Test
    @DisplayName("STATE 1 · opens_at in the future")
    void notOpenBeforeTheWindow() {
        assertEquals(RegistrationState.NOT_OPEN,
                RegistrationState.of(true, FUTURE, null, 10, 0, NOW));
    }

    @Test
    @DisplayName("opens_at is inclusive — open exactly at the boundary instant")
    void opensAtIsInclusive() {
        assertEquals(RegistrationState.OPEN,
                RegistrationState.of(true, NOW, null, 10, 0, NOW));
    }

    @Test
    @DisplayName("STATE 4 · closes_at has passed")
    void closedAfterTheWindow() {
        assertEquals(RegistrationState.CLOSED,
                RegistrationState.of(true, null, PAST, 10, 0, NOW));
    }

    @Test
    @DisplayName("closes_at is exclusive — already closed exactly at the boundary instant")
    void closesAtIsExclusive() {
        assertEquals(RegistrationState.CLOSED,
                RegistrationState.of(true, null, NOW, 10, 0, NOW));
    }

    @Test
    @DisplayName("STATE 2 · open, capacity not reached")
    void openUnderCapacity() {
        assertEquals(RegistrationState.OPEN,
                RegistrationState.of(true, PAST, FUTURE, 10, 9, NOW));
    }

    @Test
    @DisplayName("STATE 3 · full, capacity reached exactly — new submissions go to the waitlist")
    void fullAtExactCapacity() {
        assertEquals(RegistrationState.FULL,
                RegistrationState.of(true, PAST, FUTURE, 10, 10, NOW));
    }

    @Test
    @DisplayName("full, capacity exceeded")
    void fullOverCapacity() {
        assertEquals(RegistrationState.FULL,
                RegistrationState.of(true, PAST, FUTURE, 10, 11, NOW));
    }

    @Test
    @DisplayName("null capacity is unlimited — never FULL no matter the count")
    void nullCapacityIsUnlimited() {
        assertEquals(RegistrationState.OPEN,
                RegistrationState.of(true, PAST, FUTURE, null, 1_000_000, NOW));
    }

    @Test
    @DisplayName("no window at all — open the moment it's registerable")
    void noWindowConfiguredIsOpen() {
        assertEquals(RegistrationState.OPEN,
                RegistrationState.of(true, null, null, 10, 0, NOW));
    }
}
