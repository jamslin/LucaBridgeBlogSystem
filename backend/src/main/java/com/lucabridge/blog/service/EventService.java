package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.EventDetailDto;
import com.lucabridge.blog.dto.EventSummaryDto;
import com.lucabridge.blog.entity.Event;
import com.lucabridge.blog.entity.EventTranslation;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private static final String PUBLISHED = "PUBLISHED";

    private final EventRepository eventRepository;
    private final LocalizationService localizationService;

    public EventService(EventRepository eventRepository, LocalizationService localizationService) {
        this.eventRepository = eventRepository;
        this.localizationService = localizationService;
    }

    @Transactional(readOnly = true)
    public List<EventSummaryDto> listEvents(String lang) {
        String normalizedLang = localizationService.normalize(lang);
        return eventRepository.findByStatusOrderByStartsAtDesc(PUBLISHED).stream()
                .map(e -> {
                    var resolved = localizationService.resolve(
                            e.getTranslations(), normalizedLang, EventTranslation::getLang);
                    String title = resolved.map(r -> r.value().getTitle()).orElse(e.getSlug());
                    String summary = resolved.map(r -> r.value().getSummary()).orElse(null);
                    boolean fallback = resolved.map(LocalizationService.Resolved::fallback).orElse(false);
                    return new EventSummaryDto(e.getId(), e.getSlug(), title, summary,
                            e.getStartsAt(), e.getEndsAt(), e.getLocationText(), e.getCoverImageUrl(), fallback);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EventDetailDto getBySlug(String slug, String lang) {
        String normalizedLang = localizationService.normalize(lang);
        Event event = eventRepository.findBySlugAndStatus(slug, PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + slug));

        var resolved = localizationService.resolve(event.getTranslations(), normalizedLang, EventTranslation::getLang)
                .orElseThrow(() -> new ResourceNotFoundException("Event has no usable translation: " + slug));

        EventTranslation t = resolved.value();
        return new EventDetailDto(event.getId(), event.getSlug(), t.getTitle(), t.getSummary(),
                t.getBodyMarkdown(), event.getStartsAt(), event.getEndsAt(),
                event.getLocationText(), event.getCoverImageUrl(), resolved.fallback());
    }
}
