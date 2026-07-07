package com.lucabridge.blog.dto;

import java.util.List;

public record PostPageDto(
        List<PostSummaryDto> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
