package com.lucabridge.core.user.dto;

import com.lucabridge.core.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 120) String displayName,
        @Email @Size(max = 320) String email,
        @NotEmpty Set<Role> roles,
        Boolean active) {
}
