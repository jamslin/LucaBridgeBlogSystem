package com.lucabridge.core.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReferralGroupUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        Integer sortOrder,
        boolean active,
        @NotBlank @Size(max = 200) String tcName,
        @Size(max = 200) String enName,
        @Size(max = 200) String scName) {
}
