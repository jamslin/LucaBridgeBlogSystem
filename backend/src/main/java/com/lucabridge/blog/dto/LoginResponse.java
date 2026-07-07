package com.lucabridge.blog.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {}
