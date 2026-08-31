package com.lucabridge.core.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyUpsertRequest(
        @Size(max = 40) String charityRegNo,
        Integer foundedYear,
        @Size(max = 40) String phone,
        @Email @Size(max = 320) String email,
        Long logoMediaId,
        @Size(max = 500) String instagramUrl,
        @Size(max = 500) String facebookUrl,
        @Size(max = 500) String youtubeUrl,
        @NotBlank @Size(max = 200) String tcName,
        @Size(max = 200) String enName,
        @Size(max = 200) String scName,
        @Size(max = 300) String tcTagline,
        @Size(max = 300) String enTagline,
        @Size(max = 300) String scTagline,
        String tcAbout,
        String enAbout,
        String scAbout,
        @Size(max = 500) String tcAddress,
        @Size(max = 500) String enAddress,
        @Size(max = 500) String scAddress,
        @Size(max = 300) String tcOfficeHours,
        @Size(max = 300) String enOfficeHours,
        @Size(max = 300) String scOfficeHours) {
}
