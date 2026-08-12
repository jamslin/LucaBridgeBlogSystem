package com.lucabridge.blog.dto;

import java.util.List;

/** Identity of the current token holder — used by the admin SPA to gate UI by role. */
public record MeResponse(
        String username,
        List<String> roles
) {}
