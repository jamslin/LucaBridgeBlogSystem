package com.lucabridge.core.service.dto;

public record AdminServiceDto(
        Long id,
        String code,
        Integer sortOrder,
        boolean active,
        Long iconMediaId,
        String tcName,
        String enName,
        String scName,
        String tcDescription,
        String enDescription,
        String scDescription) {
}
