package com.lucabridge.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authenticated-but-wrong-role must stay 403, never 401: the admin SPA treats 401 as
     * "session expired" and redirects to login, so an EDITOR hitting an ADMIN-only action would
     * otherwise get silently logged out instead of told they lack permission. Spring's default
     * AccessDeniedHandler already sends 403 with an empty body; this one matches
     * GlobalExceptionHandler's {timestamp,status,error,message} JSON shape instead, so every
     * error response from this API looks the same regardless of which layer produced it.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
            body.put("message", "You do not have permission to perform this action");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // Stateless JWT API: no cookies, so no CSRF surface.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Unauthenticated -> 401, authenticated-but-wrong-role -> 403 (see
                // accessDeniedHandler above). Without an explicit accessDeniedHandler, Spring's
                // own default already sends 403 for a role mismatch on the FIRST pass through
                // this filter chain — the bug that used to turn that into a 401 wasn't here. It
                // was that /error had no rule of its own: Spring Boot's error handling forwards
                // internally to /error to render the response body, that forward runs back
                // through this whole chain, finds no matching rule, falls to anyRequest()
                // .authenticated() with no authentication present, and the resulting
                // AuthenticationException overwrites the already-correct 403 with 401. Exempting
                // /error stops that second pass from ever touching the status code.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()

                        // CORS preflight carries no auth header. Without this a cross-origin
                        // admin call fails as "Failed to fetch" with no useful error - this
                        // cost an afternoon of debugging once already.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Only login is public. /api/auth/me is deliberately NOT listed here —
                        // it falls through to anyRequest().authenticated() below, so a missing
                        // or invalid token 401s automatically instead of AuthController having
                        // to check for one itself.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Public read API, GET only. Every write goes through /api/admin/**.
                        .requestMatchers(HttpMethod.GET,
                                "/api/blog/**", "/api/events/**", "/api/jobs/**",
                                "/api/services/**", "/api/home/**", "/api/company",
                                "/api/media/**").permitAll()

                        // Event registration is the one place the public may WRITE. It is
                        // rate-limited and spam-guarded in the controller.
                        .requestMatchers(HttpMethod.POST, "/api/events/*/registrations").permitAll()

                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()

                        // Registrations hold names, phone numbers and home addresses. An EDITOR
                        // writes content; an EDITOR has no business reading personal data.
                        .requestMatchers("/api/admin/registrations/**").hasRole("ADMIN")

                        // User management is ADMIN-only.
                        .requestMatchers("/api/admin/users/**").hasRole("ADMIN")

                        // Deletion is ADMIN-only across every content type. Placed BEFORE the
                        // general /api/admin/** rule, which would otherwise match first and
                        // let an EDITOR through.
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")

                        // Everything else in the CMS: ADMIN or EDITOR.
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EDITOR")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
