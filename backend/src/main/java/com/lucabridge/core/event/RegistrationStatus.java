package com.lucabridge.core.event;

import java.util.Set;

/** Matches event_registration.status's CHECK constraint. Only CONFIRMED and WAITLIST are ever set by the public submission path — see EventRegistrationService. */
public enum RegistrationStatus {
    PENDING, CONFIRMED, WAITLIST, CANCELLED, ATTENDED;

    /**
     * Statuses that occupy a place against capacity. ATTENDED is included alongside CONFIRMED:
     * someone checked in at the event still occupies the place they registered for, so excluding
     * them would free capacity that isn't actually free. WAITLIST does not occupy a place — that
     * is the point of a waitlist.
     */
    public static final Set<RegistrationStatus> OCCUPIES_CAPACITY = Set.of(CONFIRMED, ATTENDED);
}
