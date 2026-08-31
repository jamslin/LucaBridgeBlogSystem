package com.lucabridge.core.event;

import java.time.Instant;

/**
 * The five registration states from the design's component sheet (7c · 五個報名狀態). This is a
 * backend contract, not styling — the frontend must never derive these from the raw event
 * fields itself. Computed in exactly one place: {@link #of}.
 *
 * <p>Precedence matches the design's own ordering: a non-registerable event is always
 * {@link #NOT_REGISTERABLE} regardless of dates; otherwise the registration window decides
 * {@link #NOT_OPEN} / {@link #CLOSED}; only within an open window does capacity decide
 * {@link #OPEN} vs {@link #FULL}.
 */
public enum RegistrationState {
    /** is_registerable = false. No button, no progress bar — just a contact link. */
    NOT_REGISTERABLE,
    /** registration_opens_at is in the future. Ghost CTA routes to volunteer signup, not a per-event notify list. */
    NOT_OPEN,
    /** Within the window, capacity not yet reached. */
    OPEN,
    /** Within the window, capacity reached — new submissions land on the waitlist. */
    FULL,
    /** registration_closes_at has passed. */
    CLOSED;

    /**
     * @param registeredCount CONFIRMED registrations only — see EventRegistrationRepository.
     */
    public static RegistrationState of(boolean isRegisterable,
                                        Instant registrationOpensAt,
                                        Instant registrationClosesAt,
                                        Integer capacity,
                                        long registeredCount,
                                        Instant now) {
        if (!isRegisterable) {
            return NOT_REGISTERABLE;
        }
        // opens_at is inclusive, closes_at is exclusive — same boundary convention as Visibility.
        if (registrationOpensAt != null && registrationOpensAt.isAfter(now)) {
            return NOT_OPEN;
        }
        if (registrationClosesAt != null && !registrationClosesAt.isAfter(now)) {
            return CLOSED;
        }
        if (capacity != null && registeredCount >= capacity) {
            return FULL;
        }
        return OPEN;
    }
}
