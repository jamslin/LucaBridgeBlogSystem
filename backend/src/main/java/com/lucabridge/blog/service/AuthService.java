package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.LoginRequest;
import com.lucabridge.blog.dto.LoginResponse;
import com.lucabridge.blog.entity.AppUser;
import com.lucabridge.blog.entity.Role;
import com.lucabridge.blog.exception.TooManyRequestsException;
import com.lucabridge.blog.exception.UnauthorizedException;
import com.lucabridge.blog.repository.AppUserRepository;
import com.lucabridge.blog.security.JwtUtil;
import com.lucabridge.blog.security.LoginRateLimiter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * DB-backed authentication (Phase A ACL). Users live in app_user with one or
 * more roles; the issued JWT carries those roles. The bootstrap admin is seeded
 * by AdminUserInitializer, so the env-var admin still logs in — as a DB row now.
 */
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final AppUserRepository userRepository;
    private final String dummyHash;

    public AuthService(JwtUtil jwtUtil,
                        PasswordEncoder passwordEncoder,
                        LoginRateLimiter rateLimiter,
                        AppUserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.userRepository = userRepository;
        // Compared against when the username is unknown, so response timing does not
        // reveal whether the account exists.
        this.dummyHash = passwordEncoder.encode("invalid-account-placeholder");
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new TooManyRequestsException("Too many login attempts — try again later");
        }

        Optional<AppUser> userOpt = userRepository.findByUsername(request.username());
        String hashToCheck = userOpt.map(AppUser::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (userOpt.isEmpty() || !passwordMatches || !userOpt.get().isEnabled()) {
            throw new UnauthorizedException("Invalid credentials");
        }

        AppUser user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
        return new LoginResponse(
                token, "Bearer", jwtUtil.getExpirationSeconds(),
                user.getUsername(), user.getRoles().stream().map(Role::name).sorted().toList());
    }
}
