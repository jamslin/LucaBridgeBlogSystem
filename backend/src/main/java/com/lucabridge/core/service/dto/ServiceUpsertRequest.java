package com.lucabridge.core.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceUpsertRequest(
        @NotBlank @Size(max = 50) String code,
        Long iconMediaId,
        Integer sortOrder,
        boolean active,
        @NotBlank @Size(max = 120) String tcName,
        @Size(max = 120) String enName,
        @Size(max = 120) String scName,
        @Size(max = 500) String tcDescription,
        @Size(max = 500) String enDescription,
        @Size(max = 500) String scDescription) {
}
