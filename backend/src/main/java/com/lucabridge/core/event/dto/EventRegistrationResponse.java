package com.lucabridge.core.event.dto;

import com.lucabridge.core.event.RegistrationStatus;

/** No confirmation email — the stack has no outbound mail. This is what the on-screen confirmation is built from. */
public record EventRegistrationResponse(String referenceCode, RegistrationStatus status) {
}
