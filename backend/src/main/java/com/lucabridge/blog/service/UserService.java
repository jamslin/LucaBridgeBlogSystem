package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.AppUser;
import com.lucabridge.blog.entity.Role;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BadRequestException("Username already exists: " + req.username());
        }
        AppUser user = AppUser.builder()
                .username(req.username().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName())
                .enabled(true)
                .roles(Set.copyOf(req.roles()))
                .build();
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest req) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        Set<Role> newRoles = req.roles() != null ? Set.copyOf(req.roles()) : user.getRoles();
        boolean newEnabled = req.enabled() != null ? req.enabled() : user.isEnabled();
        if (newRoles.isEmpty()) {
            throw new BadRequestException("A user must have at least one role");
        }
        ensureNotDroppingLastAdmin(user, newRoles, newEnabled);

        if (req.displayName() != null) {
            user.setDisplayName(req.displayName());
        }
        user.setEnabled(newEnabled);
        user.setRoles(newRoles);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        // Deleting is the same as dropping to no roles / disabled for the guard.
        ensureNotDroppingLastAdmin(user, Set.of(), false);
        userRepository.delete(user);
    }

    /** Blocks removing ADMIN from, or disabling/deleting, the only remaining active admin. */
    private void ensureNotDroppingLastAdmin(AppUser target, Set<Role> newRoles, boolean newEnabled) {
        boolean wasActiveAdmin = target.isEnabled() && target.hasRole(Role.ADMIN);
        boolean willBeActiveAdmin = newEnabled && newRoles.contains(Role.ADMIN);
        if (wasActiveAdmin && !willBeActiveAdmin) {
            long otherActiveAdmins = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(target.getId()))
                    .filter(u -> u.isEnabled() && u.hasRole(Role.ADMIN))
                    .count();
            if (otherActiveAdmins == 0) {
                throw new BadRequestException("Cannot remove the last active administrator");
            }
        }
    }

    private UserDto toDto(AppUser u) {
        List<String> roles = u.getRoles().stream().map(Role::name).sorted().toList();
        return new UserDto(u.getId(), u.getUsername(), u.getDisplayName(),
                u.isEnabled(), roles, u.getCreatedAt(), u.getUpdatedAt());
    }
}
