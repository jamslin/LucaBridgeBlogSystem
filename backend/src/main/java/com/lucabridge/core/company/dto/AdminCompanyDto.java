package com.lucabridge.core.company.dto;

import java.time.Instant;

public record AdminCompanyDto(
        String charityRegNo,
        Integer foundedYear,
        String phone,
        String email,
        Long logoMediaId,
        String instagramUrl,
        String facebookUrl,
        String youtubeUrl,
        String tcName,
        String enName,
        String scName,
        String tcTagline,
        String enTagline,
        String scTagline,
        String tcAbout,
        String enAbout,
        String scAbout,
        String tcAddress,
        String enAddress,
        String scAddress,
        String tcOfficeHours,
        String enOfficeHours,
        String scOfficeHours,
        Instant updatedAt) {
}
