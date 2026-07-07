package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.LoginRequest;
import com.lucabridge.blog.dto.LoginResponse;
import com.lucabridge.blog.exception.TooManyRequestsException;
import com.lucabridge.blog.exception.UnauthorizedException;
import com.lucabridge.blog.security.JwtUtil;
import com.lucabridge.blog.security.LoginRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Single-admin auth for v1 (per "Resolved decisions": single admin role, RBAC later).
 * Credentials come from env vars, not the DB — there is exactly one admin account.
 */
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final String adminUsername;
    private final String adminPasswordHash;

    public AuthService(JwtUtil jwtUtil,
                        PasswordEncoder passwordEncoder,
                        LoginRateLimiter rateLimiter,
                        @Value("${app.admin.username:admin}") String adminUsername,
                        @Value("${app.admin.password-hash:}") String adminPasswordHash) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = adminPasswordHash;
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new TooManyRequestsException("Too many login attempts — try again later");
        }
        if (adminPasswordHash == null || adminPasswordHash.isBlank()) {
            throw new UnauthorizedException("Admin login is not configured (set APP_ADMIN_PASSWORD_HASH)");
        }

        // Always run the BCrypt comparison, even for a wrong username, so response
        // timing doesn't reveal whether the username was correct.
        boolean passwordMatches = passwordEncoder.matches(request.password(), adminPasswordHash);
        boolean usernameMatches = adminUsername.equals(request.username());

        if (!usernameMatches || !passwordMatches) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(request.username());
        return new LoginResponse(token, "Bearer", jwtUtil.getExpirationSeconds());
    }
}
