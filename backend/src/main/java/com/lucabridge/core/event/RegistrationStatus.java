package com.lucabridge.core.event;

/** Matches event_registration.status's CHECK constraint. Only CONFIRMED and WAITLIST are ever set by the public submission path — see EventRegistrationService. */
public enum RegistrationStatus {
    PENDING, CONFIRMED, WAITLIST, CANCELLED, ATTENDED
}
