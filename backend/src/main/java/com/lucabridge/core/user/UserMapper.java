package com.lucabridge.core.user;

import com.lucabridge.core.user.dto.AdminUserDto;

final class UserMapper {

    private UserMapper() {
    }

    static AdminUserDto toDto(AppUser user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.isActive(),
                user.getRoles(),
                user.getLastLoginAt());
    }
}
