package com.lucabridge.blog.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String username,
        List<String> roles
) {}
