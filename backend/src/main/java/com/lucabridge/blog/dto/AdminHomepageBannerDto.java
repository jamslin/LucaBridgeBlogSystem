package com.lucabridge.blog.dto;

import java.time.Instant;

public record AdminHomepageBannerDto(Long id, String imageUrl, String linkUrl, int sortOrder,
        boolean active, Instant startsAt, Instant endsAt,
        String titleZhHant, String subtitleZhHant, String buttonLabelZhHant,
        String titleEn, String subtitleEn, String buttonLabelEn,
        String titleZhHans, String subtitleZhHans, String buttonLabelZhHans) {}
