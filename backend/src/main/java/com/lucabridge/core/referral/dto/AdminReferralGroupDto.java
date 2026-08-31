package com.lucabridge.core.referral.dto;

public record AdminReferralGroupDto(
        Long id,
        String code,
        Integer sortOrder,
        boolean active,
        String tcName,
        String enName,
        String scName) {
}
