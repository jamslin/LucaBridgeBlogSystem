package com.lucabridge.blog.dto;

import java.time.Instant;
import java.util.List;

public record UserDto(
        Long id,
        String username,
        String displayName,
        boolean enabled,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}
