package com.lucabridge.blog.dto;

import com.lucabridge.blog.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 60) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 120) String displayName,
        @NotEmpty Set<Role> roles
) {}
