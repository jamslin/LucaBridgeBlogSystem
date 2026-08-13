package com.lucabridge.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record HomepageBannerUpsertRequest(Long id, @NotBlank String imageUrl, String linkUrl,
        @PositiveOrZero int sortOrder, boolean active, Instant startsAt, Instant endsAt,
        @NotBlank String titleZhHant, String subtitleZhHant, String buttonLabelZhHant,
        String titleEn, String subtitleEn, String buttonLabelEn,
        String titleZhHans, String subtitleZhHans, String buttonLabelZhHans) {}
