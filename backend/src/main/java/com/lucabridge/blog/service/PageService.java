package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.PageDto;
import com.lucabridge.blog.dto.PageSummaryDto;
import com.lucabridge.blog.entity.Page;
import com.lucabridge.blog.entity.PageTranslation;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.PageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PageService {

    private static final String PUBLISHED = "PUBLISHED";

    private final PageRepository pageRepository;
    private final LocalizationService localizationService;

    public PageService(PageRepository pageRepository, LocalizationService localizationService) {
        this.pageRepository = pageRepository;
        this.localizationService = localizationService;
    }

    @Transactional(readOnly = true)
    public List<PageSummaryDto> listPages(String lang) {
        String normalizedLang = localizationService.normalize(lang);
        return pageRepository.findByStatusOrderBySortOrderAsc(PUBLISHED).stream()
                .map(p -> {
                    var resolved = localizationService.resolve(
                            p.getTranslations(), normalizedLang, PageTranslation::getLang);
                    String title = resolved.map(r -> r.value().getTitle()).orElse(p.getSlug());
                    boolean fallback = resolved.map(LocalizationService.Resolved::fallback).orElse(false);
                    return new PageSummaryDto(p.getId(), p.getSlug(), p.getPageType(), title, fallback);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PageDto getBySlug(String slug, String lang) {
        String normalizedLang = localizationService.normalize(lang);
        Page page = pageRepository.findBySlugAndStatus(slug, PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + slug));

        var resolved = localizationService.resolve(page.getTranslations(), normalizedLang, PageTranslation::getLang)
                .orElseThrow(() -> new ResourceNotFoundException("Page has no usable translation: " + slug));

        PageTranslation t = resolved.value();
        return new PageDto(page.getId(), page.getSlug(), page.getPageType(),
                t.getTitle(), t.getSubtitle(), t.getBodyMarkdown(), page.getHeroImageUrl(), resolved.fallback());
    }
}
