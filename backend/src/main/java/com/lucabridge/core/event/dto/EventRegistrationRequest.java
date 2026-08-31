package com.lucabridge.core.event.dto;

import com.lucabridge.core.event.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Fixed columns, not a form builder. terms_accepted_at/privacy_consent_at are NOT here — the
 * server stamps Instant.now() itself rather than trusting a client-supplied timestamp.
 * is_friends_opt_in is separate and optional, never coupled to the two mandatory consents.
 */
public record EventRegistrationRequest(
        @NotBlank @Size(max = 200) String fullName,
        Gender gender,
        @Min(1900) @Max(2100) Integer birthYear,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 40) String phone,
        @Size(max = 500) String postalAddress,
        Long referralGroupId,
        @Size(max = 200) String referralGroupOther,
        @NotBlank @Size(max = 2) String lang,
        @NotBlank @Size(max = 20) String termsVersion,
        @NotBlank @Size(max = 20) String privacyVersion,
        boolean friendsOptIn) {
}
