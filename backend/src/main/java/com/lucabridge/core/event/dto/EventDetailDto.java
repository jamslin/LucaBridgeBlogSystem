package com.lucabridge.core.event.dto;

import com.lucabridge.core.content.GalleryLayout;

import java.time.Instant;
import java.util.List;

public record EventDetailDto(
        Long id,
        String slug,
        String title,
        String summary,
        String body,
        String venue,
        String venueMapUrl,
        String coverUrl,
        Integer coverWidth,
        Integer coverHeight,
        Instant startsAt,
        Instant endsAt,
        Instant registrationOpensAt,
        Instant registrationClosesAt,
        GalleryLayout galleryLayout,
        List<GalleryImageDto> gallery,
        RegistrationInfoDto registration) {
}
