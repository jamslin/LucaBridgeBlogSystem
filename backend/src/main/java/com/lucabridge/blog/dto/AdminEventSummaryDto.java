package com.lucabridge.blog.dto;

import java.time.Instant;

public record AdminEventSummaryDto(
        Long id,
        String slug,
        String status,
        String title,
        Instant startsAt
) {}
