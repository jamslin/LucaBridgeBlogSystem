package com.lucabridge.core.event;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.event.dto.EventUpsertRequest;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.MediaRepository;
import com.lucabridge.core.publish.PublishStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final MediaRepository mediaRepository;

    public EventService(EventRepository eventRepository, EventRegistrationRepository registrationRepository,
                         MediaRepository mediaRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.mediaRepository = mediaRepository;
    }

    // ---- public ----

    /** totalElements on the returned Page IS "how many upcoming" — the home page needs nothing else to pick its 0/1/many layout. */
    @Transactional(readOnly = true)
    public Page<Event> listUpcoming(Pageable pageable) {
        return eventRepository.findUpcomingVisible(Instant.now(), pageable);
    }

    @Transactional(readOnly = true)
    public Event getPublishedBySlug(String slug) {
        return eventRepository.findVisibleBySlug(slug, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + slug));
    }

    // ---- admin ----

    @Transactional(readOnly = true)
    public Page<Event> listActive(Pageable pageable) {
        return eventRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public Event getActiveById(Long id) {
        return eventRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean existsActive(Long id) {
        return eventRepository.existsByIdAndDeletedAtIsNull(id);
    }

    @Transactional
    public Event create(EventUpsertRequest req, Long currentUserId) {
        Event event = Event.builder()
                .slug(req.slug())
                .galleryLayout(req.galleryLayout() == null ? GalleryLayout.NONE : req.galleryLayout())
                .startsAt(req.startsAt())
                .endsAt(req.endsAt())
                .venueMapUrl(req.venueMapUrl())
                .registerable(req.registerable())
                .registrationOpensAt(req.registrationOpensAt())
                .registrationClosesAt(req.registrationClosesAt())
                .capacity(req.capacity())
                .status(req.status())
                .publishAt(req.publishAt())
                .unpublishAt(req.unpublishAt())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        applyCoverMedia(event, req.coverMediaId());
        applyText(event, req);
        applyGallery(event, req.galleryMediaIds());
        applyFirstPublish(event);
        return save(event);
    }

    @Transactional
    public Event update(Long id, EventUpsertRequest req, Long currentUserId) {
        Event event = eventRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        event.setSlug(req.slug());
        event.setGalleryLayout(req.galleryLayout() == null ? GalleryLayout.NONE : req.galleryLayout());
        event.setStartsAt(req.startsAt());
        event.setEndsAt(req.endsAt());
        event.setVenueMapUrl(req.venueMapUrl());
        event.setRegisterable(req.registerable());
        event.setRegistrationOpensAt(req.registrationOpensAt());
        event.setRegistrationClosesAt(req.registrationClosesAt());
        event.setCapacity(req.capacity());
        event.setStatus(req.status());
        event.setPublishAt(req.publishAt());
        event.setUnpublishAt(req.unpublishAt());
        event.setUpdatedBy(currentUserId);
        applyCoverMedia(event, req.coverMediaId());
        applyText(event, req);
        applyGallery(event, req.galleryMediaIds());
        applyFirstPublish(event);
        return save(event);
    }

    /** Soft delete only: sets deleted_at. Never hard-deleted — media_usage depends on it still counting, and event_registration.event_id is ON DELETE RESTRICT anyway. */
    @Transactional
    public void softDelete(Long id, Long currentUserId) {
        Event event = eventRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        event.setDeletedAt(Instant.now());
        event.setUpdatedBy(currentUserId);
    }

    /**
     * One aggregate query for a whole page of events, not N+1 per row. No application cache: the
     * count changes on every submission and this traffic volume doesn't justify the staleness
     * risk of a cache, so every request queries live.
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> confirmedCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return registrationRepository.countConfirmedByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(
                        EventRegistrationRepository.ConfirmedCount::getEventId,
                        EventRegistrationRepository.ConfirmedCount::getConfirmedCount));
    }

    @Transactional(readOnly = true)
    public long confirmedCount(Long eventId) {
        return registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }

    private void applyCoverMedia(Event event, Long coverMediaId) {
        if (coverMediaId == null) {
            event.setCoverMedia(null);
            return;
        }
        Media media = mediaRepository.findById(coverMediaId)
                .orElseThrow(() -> new BadRequestException("Unknown media: " + coverMediaId));
        event.setCoverMedia(media);
    }

    private void applyText(Event event, EventUpsertRequest req) {
        EventText text = event.getText();
        if (text == null) {
            text = new EventText();
            event.setText(text);
            text.setEvent(event);
        }
        text.setTcTitle(req.tcTitle());
        text.setEnTitle(req.enTitle());
        text.setScTitle(req.scTitle());
        text.setTcSummary(req.tcSummary());
        text.setEnSummary(req.enSummary());
        text.setScSummary(req.scSummary());
        text.setTcBody(req.tcBody());
        text.setEnBody(req.enBody());
        text.setScBody(req.scBody());
        text.setTcVenue(req.tcVenue());
        text.setEnVenue(req.enVenue());
        text.setScVenue(req.scVenue());
    }

    /** Clears and rebuilds the collection rather than diffing — orphanRemoval handles the deletes. */
    private void applyGallery(Event event, List<Long> mediaIds) {
        event.getGallery().clear();
        if (mediaIds == null) {
            return;
        }
        int order = 0;
        for (Long mediaId : mediaIds) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new BadRequestException("Unknown media: " + mediaId));
            event.getGallery().add(EventGallery.builder()
                    .event(event)
                    .media(media)
                    .sortOrder(order++)
                    .build());
        }
    }

    /** published_at is set once, on first publish, and never moved by later edits. */
    private void applyFirstPublish(Event event) {
        if (event.getStatus() == PublishStatus.PUBLISHED && event.getPublishedAt() == null) {
            event.setPublishedAt(Instant.now());
        }
    }

    /** Forces the flush so a slug conflict surfaces here as a clean ConflictException — same deferred-write trap as BlogService.save. */
    private Event save(Event event) {
        try {
            Event saved = eventRepository.save(event);
            eventRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Slug already in use: " + event.getSlug());
        }
    }
}
