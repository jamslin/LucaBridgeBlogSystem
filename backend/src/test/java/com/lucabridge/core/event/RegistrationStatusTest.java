package com.lucabridge.core.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pins which statuses occupy a place against capacity — ATTENDED included, WAITLIST excluded. */
class RegistrationStatusTest {

    @Test
    void confirmedAndAttendedOccupyCapacity() {
        assertTrue(RegistrationStatus.OCCUPIES_CAPACITY.contains(RegistrationStatus.CONFIRMED));
        assertTrue(RegistrationStatus.OCCUPIES_CAPACITY.contains(RegistrationStatus.ATTENDED));
    }

    @Test
    void waitlistPendingAndCancelledDoNotOccupyCapacity() {
        assertFalse(RegistrationStatus.OCCUPIES_CAPACITY.contains(RegistrationStatus.WAITLIST));
        assertFalse(RegistrationStatus.OCCUPIES_CAPACITY.contains(RegistrationStatus.PENDING));
        assertFalse(RegistrationStatus.OCCUPIES_CAPACITY.contains(RegistrationStatus.CANCELLED));
    }
}
