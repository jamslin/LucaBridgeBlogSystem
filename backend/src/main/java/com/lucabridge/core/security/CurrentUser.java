package com.lucabridge.core.security;

import com.lucabridge.core.error.UnauthorizedException;
import com.lucabridge.core.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The JWT carries only the username (see {@link JwtUtil#generateToken}), but every admin write
 * needs an {@code app_user.id} for {@code created_by}/{@code updated_by}. This is the one place
 * that resolves it, shared by every admin controller rather than each doing its own lookup.
 */
@Component
public class CurrentUser {

    private final AppUserRepository appUserRepository;

    public CurrentUser(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Unknown user: " + auth.getName()))
                .getId();
    }
}
