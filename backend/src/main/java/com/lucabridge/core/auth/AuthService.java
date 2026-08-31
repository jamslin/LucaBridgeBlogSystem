package com.lucabridge.core.auth;

import com.lucabridge.core.error.TooManyRequestsException;
import com.lucabridge.core.error.UnauthorizedException;
import com.lucabridge.core.security.JwtUtil;
import com.lucabridge.core.security.LoginRateLimiter;
import com.lucabridge.core.user.AppUser;
import com.lucabridge.core.user.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    /**
     * Not a real account's hash — checked against every login for an unknown username, so
     * verifying one costs the same BCrypt round as a real user and response time alone can't
     * reveal whether the username exists.
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2b$10$gB9Ge2h5niuYrKyj0ylpvOA16axZh72yt1avlK4nWpILQ24tGqqEy";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter rateLimiter;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, LoginRateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Always runs a BCrypt comparison, win or lose, against either the real hash or
     * DUMMY_PASSWORD_HASH for an unknown username — never short-circuits on "user not found"
     * before that comparison. Every failure path (unknown user, wrong password, disabled
     * account) returns the same generic message, so nothing about why it failed leaks either.
     */
    @Transactional
    public LoginResponse login(String username, String password, String clientKey) {
        if (!rateLimiter.tryAcquire(clientKey)) {
            throw new TooManyRequestsException("Too many login attempts — please try again later");
        }

        Optional<AppUser> found = userRepository.findByUsername(username);
        String hashToCheck = found.map(AppUser::getPasswordHash).orElse(DUMMY_PASSWORD_HASH);
        boolean passwordMatches = passwordEncoder.matches(password, hashToCheck);

        if (found.isEmpty() || !passwordMatches || !found.get().isActive()) {
            throw new UnauthorizedException("Invalid username or password");
        }

        AppUser user = found.get();
        user.setLastLoginAt(Instant.now());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
        return new LoginResponse(token, user.getUsername(),
                user.getRoles().stream().map(Enum::name).toList(),
                jwtUtil.getExpirationSeconds());
    }
}
