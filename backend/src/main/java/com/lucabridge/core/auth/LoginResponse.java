package com.lucabridge.core.auth;

import java.util.List;

public record LoginResponse(String token, String username, List<String> roles, long expiresInSeconds) {
}
