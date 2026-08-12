package com.lucabridge.blog.config;

import com.lucabridge.blog.entity.AppUser;
import com.lucabridge.blog.entity.Role;
import com.lucabridge.blog.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * Seeds the bootstrap admin on startup so there is always a way in.
 *
 * Priority for the admin password:
 *   1. APP_ADMIN_PASSWORD_HASH (a BCrypt hash) — the existing prod secret. Reused
 *      verbatim, so the current prod login keeps working with the same password.
 *   2. dev profile only: falls back to admin/admin123 (+ editor/editor123) so a
 *      fresh dev DB has instant logins. Never happens in prod.
 *
 * Runs only when the account is missing, so it never clobbers a changed password.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String adminUsername;
    private final String adminPasswordHash;

    public AdminUserInitializer(AppUserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                Environment environment,
                                @Value("${app.admin.username:admin}") String adminUsername,
                                @Value("${app.admin.password-hash:}") String adminPasswordHash) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = adminPasswordHash;
    }

    @Override
    public void run(String... args) {
        boolean envAdminConfigured = adminPasswordHash != null && !adminPasswordHash.isBlank();

        if (envAdminConfigured && !userRepository.existsByUsername(adminUsername)) {
            userRepository.save(AppUser.builder()
                    .username(adminUsername)
                    .passwordHash(adminPasswordHash.trim())
                    .displayName("Administrator")
                    .enabled(true)
                    .roles(Set.of(Role.ADMIN))
                    .build());
            log.info("Seeded bootstrap admin '{}' from APP_ADMIN_PASSWORD_HASH", adminUsername);
        }

        if (userRepository.count() == 0) {
            boolean dev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
            if (dev) {
                userRepository.save(AppUser.builder()
                        .username("admin").passwordHash(passwordEncoder.encode("admin123"))
                        .displayName("Dev Admin").enabled(true).roles(Set.of(Role.ADMIN)).build());
                userRepository.save(AppUser.builder()
                        .username("editor").passwordHash(passwordEncoder.encode("editor123"))
                        .displayName("Dev Editor").enabled(true).roles(Set.of(Role.EDITOR)).build());
                log.warn("DEV seed: created admin/admin123 (ADMIN) and editor/editor123 (EDITOR). Do NOT use in prod.");
            } else {
                log.warn("No CMS admin exists and APP_ADMIN_PASSWORD_HASH is unset — admin login is disabled. "
                        + "Set the hash and restart to create the bootstrap admin.");
            }
        }
    }
}
