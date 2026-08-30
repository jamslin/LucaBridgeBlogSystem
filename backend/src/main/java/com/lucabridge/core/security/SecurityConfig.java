package com.lucabridge.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JWT API: no cookies, so no CSRF surface.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Unauthenticated -> 401 rather than Spring's default 403, so an expired token
                // makes the admin SPA redirect to login. Authenticated-but-wrong-role stays 403.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight carries no auth header. Without this a cross-origin
                        // admin call fails as "Failed to fetch" with no useful error - this
                        // cost an afternoon of debugging once already.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

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
