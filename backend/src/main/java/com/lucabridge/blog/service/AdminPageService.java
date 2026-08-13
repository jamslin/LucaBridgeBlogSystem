package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.Page;
import com.lucabridge.blog.entity.PageTranslation;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ConflictException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.PageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Admin CRUD for static Pages. */
@Service
public class AdminPageService {

    private final PageRepository repo;
    private final LocalizationService localization;

    public AdminPageService(PageRepository repo, LocalizationService localization) {
        this.repo = repo;
        this.localization = localization;
    }

    @Transactional(readOnly = true)
    public List<AdminPageSummaryDto> list() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Page::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Page::getId))
                .map(p -> new AdminPageSummaryDto(p.getId(), p.getSlug(), p.getStatus(), p.getPageType(), p.getSortOrder(), titleOf(p)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPageDetailDto getForEdit(Long id) {
        Page p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        List<PageTranslationInput> tr = p.getTranslations().stream()
                .map(t -> new PageTranslationInput(t.getLang(), t.getTitle(), t.getSubtitle(), t.getBodyMarkdown()))
                .toList();
        return new AdminPageDetailDto(p.getId(), p.getSlug(), p.getStatus(), p.getPageType(), p.getSortOrder(), p.getHeroImageUrl(), tr);
    }

    @Transactional
    public Long upsert(PageUpsertRequest req) {
        Page p = req.id() != null
                ? repo.findById(req.id()).orElseThrow(() -> new ResourceNotFoundException("Page not found: " + req.id()))
                : Page.builder().status("DRAFT").build();
        String slug = req.slug().trim();
        boolean duplicate = p.getId() == null ? repo.existsBySlug(slug) : repo.existsBySlugAndIdNot(slug, p.getId());
        if (duplicate) throw new ConflictException("Page slug already exists: " + slug);
        p.setSlug(slug);
        p.setStatus("DRAFT");
        if (req.pageType() != null && !req.pageType().isBlank()) p.setPageType(req.pageType());
        p.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        p.setHeroImageUrl(req.heroImageUrl());

        Map<String, PageTranslation> existing = new HashMap<>();
        for (PageTranslation t : p.getTranslations()) existing.put(t.getLang(), t);
        Set<String> langs = new HashSet<>();
        for (PageTranslationInput in : req.translations()) {
            if (in.title() == null || in.title().isBlank()) continue;
            langs.add(in.lang());
            PageTranslation t = existing.get(in.lang());
            if (t == null) { t = PageTranslation.builder().page(p).lang(in.lang()).build(); p.getTranslations().add(t); }
            t.setTitle(in.title());
            t.setSubtitle(in.subtitle());
            t.setBodyMarkdown(in.bodyMarkdown() != null ? in.bodyMarkdown() : "");
        }
        if (langs.isEmpty()) throw new BadRequestException("At least one translation with a title is required");
        p.getTranslations().removeIf(t -> !langs.contains(t.getLang()));
        return repo.save(p).getId();
    }

    @Transactional
    public void publish(Long id) {
        Page p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        validateTranslationsForPublish(p.getTranslations());
        p.setStatus("PUBLISHED");
        repo.save(p);
    }

    @Transactional
    public void unpublish(Long id) {
        Page p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        p.setStatus("DRAFT");
        repo.save(p);
    }

    private void validateTranslationsForPublish(List<PageTranslation> translations) {
        PageTranslation required = translations.stream().filter(t -> localization.defaultLang().equals(t.getLang()))
                .findFirst().orElseThrow(() -> new BadRequestException("Cannot publish without a " + localization.defaultLang() + " translation"));
        if (!complete(required)) throw new BadRequestException("The " + localization.defaultLang() + " translation needs a title and body");
        if (translations.stream().anyMatch(t -> !complete(t))) throw new BadRequestException("Every saved translation needs a title and body before publishing");
    }

    private boolean complete(PageTranslation t) {
        return t.getTitle() != null && !t.getTitle().isBlank() && t.getBodyMarkdown() != null && !t.getBodyMarkdown().isBlank();
    }

    @Transactional
    public void delete(Long id) {
        Page p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        repo.delete(p);
    }

    private String titleOf(Page p) {
        String def = localization.defaultLang();
        String fallback = null;
        for (PageTranslation t : p.getTranslations()) {
            if (fallback == null) fallback = t.getTitle();
            if (def.equals(t.getLang())) return t.getTitle();
        }
        return fallback != null ? fallback : p.getSlug();
    }
}
