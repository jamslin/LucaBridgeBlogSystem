package com.lucabridge.core.user.dto;

import com.lucabridge.core.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** No username (identity, immutable) and no password (its own endpoint — see ChangePasswordRequest). */
public record UpdateUserRequest(
        @Size(max = 120) String displayName,
        @Email @Size(max = 320) String email,
        @NotEmpty Set<Role> roles,
        @NotNull Boolean active) {
}
