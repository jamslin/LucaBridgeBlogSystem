package com.lucabridge.core.event;

import com.lucabridge.core.event.dto.AdminEventDetailDto;
import com.lucabridge.core.event.dto.AdminEventRegistrationDto;
import com.lucabridge.core.event.dto.AdminEventSummaryDto;
import com.lucabridge.core.event.dto.EventDetailDto;
import com.lucabridge.core.event.dto.EventSummaryDto;
import com.lucabridge.core.event.dto.GalleryImageDto;
import com.lucabridge.core.event.dto.RegistrationInfoDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;
import java.util.List;

/**
 * Entity -> DTO only. Every translated field resolves through {@link Localized#pick}. Every
 * confirmed-registration count is passed in rather than queried here, so a page of summaries
 * costs one aggregate query, not one per row — see EventService.
 */
final class EventMapper {

    private EventMapper() {
    }

    static EventSummaryDto toSummary(Event event, Lang lang, long confirmedCount, Instant now) {
        EventText t = event.getText();
        Media cover = event.getCoverMedia();
        return new EventSummaryDto(
                event.getId(),
                event.getSlug(),
                title(t, lang),
                summary(t, lang),
                venue(t, lang),
                cover == null ? null : cover.getUrl(),
                cover == null ? null : cover.getWidth(),
                cover == null ? null : cover.getHeight(),
                event.getStartsAt(),
                event.getEndsAt(),
                registrationInfo(event, confirmedCount, now));
    }

    static EventDetailDto toDetail(Event event, Lang lang, long confirmedCount, Instant now) {
        EventText t = event.getText();
        Media cover = event.getCoverMedia();
        List<GalleryImageDto> gallery = event.getGallery().stream()
                .map(g -> new GalleryImageDto(g.getMedia().getUrl(), g.getMedia().getWidth(), g.getMedia().getHeight()))
                .toList();
        return new EventDetailDto(
                event.getId(),
                event.getSlug(),
                title(t, lang),
                summary(t, lang),
                t == null ? null : Localized.pick(lang, t.getTcBody(), t.getEnBody(), t.getScBody()),
                venue(t, lang),
                event.getVenueMapUrl(),
                cover == null ? null : cover.getUrl(),
                cover == null ? null : cover.getWidth(),
                cover == null ? null : cover.getHeight(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getRegistrationOpensAt(),
                event.getRegistrationClosesAt(),
                event.getGalleryLayout(),
                gallery,
                registrationInfo(event, confirmedCount, now));
    }

    static AdminEventSummaryDto toAdminSummary(Event event, long confirmedCount, Instant now) {
        EventText t = event.getText();
        return new AdminEventSummaryDto(
                event.getId(),
                event.getSlug(),
                t == null ? null : t.getTcTitle(),
                event.getStatus(),
                stateOf(event, now),
                event.getStartsAt(),
                event.getUpdatedAt(),
                registrationInfo(event, confirmedCount, now));
    }

    static AdminEventDetailDto toAdminDetail(Event event, long confirmedCount, Instant now) {
        EventText t = event.getText();
        Media cover = event.getCoverMedia();
        List<Long> galleryIds = event.getGallery().stream().map(g -> g.getMedia().getId()).toList();
        return new AdminEventDetailDto(
                event.getId(),
                event.getSlug(),
                cover == null ? null : cover.getId(),
                event.getGalleryLayout(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getVenueMapUrl(),
                event.isRegisterable(),
                event.getRegistrationOpensAt(),
                event.getRegistrationClosesAt(),
                event.getCapacity(),
                event.getStatus(),
                stateOf(event, now),
                event.getPublishAt(),
                event.getUnpublishAt(),
                event.getPublishedAt(),
                t == null ? null : t.getTcTitle(),
                t == null ? null : t.getEnTitle(),
                t == null ? null : t.getScTitle(),
                t == null ? null : t.getTcSummary(),
                t == null ? null : t.getEnSummary(),
                t == null ? null : t.getScSummary(),
                t == null ? null : t.getTcBody(),
                t == null ? null : t.getEnBody(),
                t == null ? null : t.getScBody(),
                t == null ? null : t.getTcVenue(),
                t == null ? null : t.getEnVenue(),
                t == null ? null : t.getScVenue(),
                galleryIds,
                registrationInfo(event, confirmedCount, now));
    }

    static AdminEventRegistrationDto toAdminRegistration(EventRegistration r) {
        return new AdminEventRegistrationDto(
                r.getId(),
                r.getEventId(),
                r.getReferenceCode(),
                r.getFullName(),
                r.getGender(),
                r.getBirthYear(),
                r.getEmail(),
                r.getPhone(),
                r.getPostalAddress(),
                r.getReferralGroupId(),
                r.getReferralGroupOther(),
                r.isFriendsOptIn(),
                r.isWhatsappConfirmed(),
                r.getStatus(),
                r.getLocale(),
                r.getSubmittedAt());
    }

    /** almostFull is only ever true in OPEN — a full or not-yet-open event has no "almost full" badge to show. */
    private static RegistrationInfoDto registrationInfo(Event event, long confirmedCount, Instant now) {
        RegistrationState state = RegistrationState.of(event.isRegisterable(), event.getRegistrationOpensAt(),
                event.getRegistrationClosesAt(), event.getCapacity(), confirmedCount, now);
        Integer capacity = event.getCapacity();
        Integer remaining = capacity == null ? null : Math.max(capacity - (int) confirmedCount, 0);
        boolean almostFull = state == RegistrationState.OPEN && capacity != null
                && remaining != null && remaining * 5 <= capacity;
        return new RegistrationInfoDto(state, capacity, confirmedCount, remaining, almostFull);
    }

    private static String title(EventText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcTitle(), t.getEnTitle(), t.getScTitle());
    }

    private static String summary(EventText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcSummary(), t.getEnSummary(), t.getScSummary());
    }

    private static String venue(EventText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcVenue(), t.getEnVenue(), t.getScVenue());
    }

    private static Visibility.State stateOf(Event event, Instant now) {
        return Visibility.stateOf(event.getStatus(), event.getPublishAt(), event.getUnpublishAt(),
                event.getDeletedAt(), now);
    }
}
