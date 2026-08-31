package com.lucabridge.core.auth;

import java.util.List;

public record MeResponse(String username, List<String> roles) {
}
