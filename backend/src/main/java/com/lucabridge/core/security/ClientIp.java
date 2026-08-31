package com.lucabridge.core.security;

import jakarta.servlet.http.HttpServletRequest;

/** Shared by every rate limiter. Prefers X-Forwarded-For (set by the AKS nginx ingress) over the raw connection address. */
public final class ClientIp {

    private ClientIp() {
    }

    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
