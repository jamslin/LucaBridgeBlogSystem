package com.lucabridge.core.user;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.user.dto.CreateUserRequest;
import com.lucabridge.core.user.dto.UpdateUserRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> list() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public AppUser create(CreateUserRequest req) {
        AppUser user = AppUser.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName())
                .email(req.email())
                .active(req.active() == null || req.active())
                .roles(new HashSet<>(req.roles()))
                .build();
        try {
            AppUser saved = userRepository.save(user);
            userRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Username already in use: " + req.username());
        }
    }

    /**
     * Refuses to remove the ADMIN role from, or disable, the last enabled admin — checked
     * against the user's state BEFORE this edit, so it catches exactly the edit that would
     * remove that last guarantee, not edits to some other admin.
     */
    @Transactional
    public AppUser update(Long id, UpdateUserRequest req) {
        AppUser user = get(id);
        boolean stayingEnabledAdmin = req.active() && req.roles().contains(Role.ADMIN);
        if (isLastEnabledAdmin(user) && !stayingEnabledAdmin) {
            throw new ConflictException("Cannot remove ADMIN or disable the last enabled admin");
        }
        user.setDisplayName(req.displayName());
        user.setEmail(req.email());
        user.setActive(req.active());
        user.setRoles(new HashSet<>(req.roles()));
        return user;
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        AppUser user = get(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = get(id);
        if (isLastEnabledAdmin(user)) {
            throw new ConflictException("Cannot delete the last enabled admin");
        }
        userRepository.delete(user);
    }

    private boolean isLastEnabledAdmin(AppUser user) {
        return user.isActive() && user.hasRole(Role.ADMIN) && userRepository.countActiveAdmins() <= 1;
    }
}
