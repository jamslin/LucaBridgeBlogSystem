package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.Event;
import com.lucabridge.blog.entity.EventTranslation;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Admin CRUD for Events (raw translations, all statuses). Mirrors the Posts admin path. */
@Service
public class AdminEventService {

    private final EventRepository repo;
    private final LocalizationService localization;

    public AdminEventService(EventRepository repo, LocalizationService localization) {
        this.repo = repo;
        this.localization = localization;
    }

    @Transactional(readOnly = true)
    public List<AdminEventSummaryDto> list() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Event::getId).reversed())
                .map(e -> new AdminEventSummaryDto(e.getId(), e.getSlug(), e.getStatus(), titleOf(e), e.getStartsAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminEventDetailDto getForEdit(Long id) {
        Event e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        List<EventTranslationInput> tr = e.getTranslations().stream()
                .map(t -> new EventTranslationInput(t.getLang(), t.getTitle(), t.getSummary(), t.getBodyMarkdown()))
                .toList();
        return new AdminEventDetailDto(e.getId(), e.getSlug(), e.getStatus(), e.getStartsAt(), e.getEndsAt(),
                e.getLocationText(), e.getCoverImageUrl(), tr);
    }

    @Transactional
    public Long upsert(EventUpsertRequest req) {
        Event e = req.id() != null
                ? repo.findById(req.id()).orElseThrow(() -> new ResourceNotFoundException("Event not found: " + req.id()))
                : Event.builder().status("DRAFT").build();
        e.setSlug(req.slug().trim());
        e.setStartsAt(req.startsAt());
        e.setEndsAt(req.endsAt());
        e.setLocationText(req.locationText());
        e.setCoverImageUrl(req.coverImageUrl());

        Map<String, EventTranslation> existing = new HashMap<>();
        for (EventTranslation t : e.getTranslations()) existing.put(t.getLang(), t);
        Set<String> langs = new HashSet<>();
        for (EventTranslationInput in : req.translations()) {
            if (in.title() == null || in.title().isBlank()) continue;
            langs.add(in.lang());
            EventTranslation t = existing.get(in.lang());
            if (t == null) { t = EventTranslation.builder().event(e).lang(in.lang()).build(); e.getTranslations().add(t); }
            t.setTitle(in.title());
            t.setSummary(in.summary());
            t.setBodyMarkdown(in.bodyMarkdown() != null ? in.bodyMarkdown() : "");
        }
        if (langs.isEmpty()) throw new BadRequestException("At least one translation with a title is required");
        e.getTranslations().removeIf(t -> !langs.contains(t.getLang()));
        return repo.save(e).getId();
    }

    @Transactional
    public void setStatus(Long id, String status) {
        Event e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        e.setStatus(status);
        repo.save(e);
    }

    @Transactional
    public void delete(Long id) {
        Event e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        repo.delete(e);
    }

    private String titleOf(Event e) {
        String def = localization.defaultLang();
        String fallback = null;
        for (EventTranslation t : e.getTranslations()) {
            if (fallback == null) fallback = t.getTitle();
            if (def.equals(t.getLang())) return t.getTitle();
        }
        return fallback != null ? fallback : e.getSlug();
    }
}
