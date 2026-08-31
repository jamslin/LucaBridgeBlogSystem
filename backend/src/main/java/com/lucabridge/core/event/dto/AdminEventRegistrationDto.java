package com.lucabridge.core.event.dto;

import com.lucabridge.core.event.Gender;
import com.lucabridge.core.event.RegistrationStatus;

import java.time.Instant;

/** ADMIN-only — carries phone/address. Never returned to an EDITOR; see SecurityConfig. */
public record AdminEventRegistrationDto(
        Long id,
        Long eventId,
        String referenceCode,
        String fullName,
        Gender gender,
        Integer birthYear,
        String email,
        String phone,
        String postalAddress,
        Long referralGroupId,
        String referralGroupOther,
        boolean friendsOptIn,
        boolean whatsappConfirmed,
        RegistrationStatus status,
        String locale,
        Instant submittedAt) {
}
