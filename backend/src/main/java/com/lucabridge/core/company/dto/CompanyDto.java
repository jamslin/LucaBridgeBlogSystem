package com.lucabridge.core.company.dto;

/** Public view — one language, already resolved via Localized.pick(). Shown on every page. */
public record CompanyDto(
        String name,
        String tagline,
        String about,
        String address,
        String officeHours,
        String charityRegNo,
        Integer foundedYear,
        String phone,
        String email,
        String logoUrl,
        String instagramUrl,
        String facebookUrl,
        String youtubeUrl) {
}
