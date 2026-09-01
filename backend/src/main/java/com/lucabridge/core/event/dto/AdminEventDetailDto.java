package com.lucabridge.core.event.dto;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;
import com.lucabridge.core.media.dto.MediaRefDto;

import java.util.List;

public record AdminEventDetailDto(
        Long id,
        String slug,
        Long serviceId,
        Long coverMediaId,
        GalleryLayout galleryLayout,
        Instant startsAt,
        Instant endsAt,
        String venueMapUrl,
        boolean registerable,
        Instant registrationOpensAt,
        Instant registrationClosesAt,
        Integer capacity,
        PublishStatus status,
        Visibility.State state,
        Instant publishAt,
        Instant unpublishAt,
        Instant publishedAt,
        String tcTitle,
        String enTitle,
        String scTitle,
        String tcSummary,
        String enSummary,
        String scSummary,
        String tcBody,
        String enBody,
        String scBody,
        String tcVenue,
        String enVenue,
        String scVenue,
        List<Long> galleryMediaIds,
        /** Same images as galleryMediaIds, with URLs so the editor can preview them. */
        List<MediaRefDto> galleryMedia,
        RegistrationInfoDto registration) {
}
