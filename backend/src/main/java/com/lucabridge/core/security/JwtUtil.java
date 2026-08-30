package com.lucabridge.core.security;

import com.lucabridge.core.config.AppProperties;
import com.lucabridge.core.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final AppProperties appProperties;
    private final SecretKey key;

    public JwtUtil(AppProperties appProperties) {
        this.appProperties = appProperties;
        // NOTE: HS256 needs >= 256-bit secret. app.jwt.secret must be a long random string in prod (see application.yml).
        this.key = Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** Roles are baked into the token, so the filter needs no DB lookup per request. */
    public String generateToken(String username, Collection<Role> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(appProperties.getJwt().getExpirationMinutes(), ChronoUnit.MINUTES);
        List<String> roleNames = roles.stream().map(Enum::name).toList();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roleNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public long getExpirationSeconds() {
        return appProperties.getJwt().getExpirationMinutes() * 60;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object raw = parseClaims(token).get("roles");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
