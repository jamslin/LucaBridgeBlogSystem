package com.lucabridge.blog.dto;

import com.lucabridge.blog.entity.Role;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Partial update — null fields are left unchanged. Password has its own endpoint. */
public record UpdateUserRequest(
        @Size(max = 120) String displayName,
        Boolean enabled,
        Set<Role> roles
) {}
