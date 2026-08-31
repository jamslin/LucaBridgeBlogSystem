package com.lucabridge.core.referral.dto;

/** Public view — one language, already resolved. Populates the registration form's dropdown. */
public record ReferralGroupDto(Long id, String code, String name) {
}
