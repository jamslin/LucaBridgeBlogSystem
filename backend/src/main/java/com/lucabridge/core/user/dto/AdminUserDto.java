package com.lucabridge.core.user.dto;

import com.lucabridge.core.user.Role;

import java.time.Instant;
import java.util.Set;

/** Never carries a password or its hash. */
public record AdminUserDto(
        Long id,
        String username,
        String displayName,
        String email,
        boolean active,
        Set<Role> roles,
        Instant lastLoginAt) {
}
