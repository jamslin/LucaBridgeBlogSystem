package com.lucabridge.blog.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Fails fast at startup in prod if JWT_SECRET was left at the insecure default
 * or is too short for HS256 (< 256 bits). A silently-forgeable token is worse
 * than a crashed deploy.
 */
@Component
@Profile("prod")
public class JwtSecretValidator {

    static final String INSECURE_DEFAULT = "change-me-in-prod-this-should-be-a-long-random-string";

    private final AppProperties appProperties;

    public JwtSecretValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    void validate() {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.equals(INSECURE_DEFAULT)) {
            throw new IllegalStateException(
                    "JWT_SECRET is unset or still the default — refusing to start in prod. "
                    + "Generate one with: openssl rand -base64 48");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET is shorter than 256 bits — too weak for HS256. "
                    + "Generate one with: openssl rand -base64 48");
        }
    }
}
