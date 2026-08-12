package com.lucabridge.blog.dto;

import java.time.Instant;

public record AdminJobSummaryDto(
        Long id,
        String slug,
        String status,
        String title,
        String department,
        Instant postedAt
) {}
