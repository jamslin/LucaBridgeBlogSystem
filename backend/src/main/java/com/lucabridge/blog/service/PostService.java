package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.*;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.CategoryRepository;
import com.lucabridge.blog.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final LocalizationService localizationService;

    public PostService(PostRepository postRepository,
                        CategoryRepository categoryRepository,
                        CategoryService categoryService,
                        LocalizationService localizationService) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.localizationService = localizationService;
    }

    @Transactional(readOnly = true)
    public PostPageDto listPublished(String lang, String categoryKey, int page, int size) {
        String normalizedLang = localizationService.normalize(lang);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "publishedAt"));

        Page<Post> result = (categoryKey == null || categoryKey.isBlank())
                ? postRepository.findByStatus(PostStatus.PUBLISHED, pageable)
                : postRepository.findByStatusAndCategoryKey(PostStatus.PUBLISHED, categoryKey, pageable);

        List<PostSummaryDto> items = result.getContent().stream()
                .map(p -> toSummaryDto(p, normalizedLang))
                .toList();

        return new PostPageDto(items, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPublishedBySlug(String slug, String lang) {
        String normalizedLang = localizationService.normalize(lang);
        Post post = postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + slug));

        var resolved = localizationService.resolve(post.getTranslations(), normalizedLang, PostTranslation::getLang)
                .orElseThrow(() -> new ResourceNotFoundException("Post has no usable translation: " + slug));

        PostTranslation translation = resolved.value();

        List<MediaDto> mediaDtos = post.getMedia().stream()
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .map(m -> new MediaDto(m.getId(), m.getUrl(), m.getWidth(), m.getHeight(), m.getCaption(), m.getSortOrder()))
                .toList();

        List<PressLinkDto> pressLinkDtos = post.getPressLinks().stream()
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .map(pl -> new PressLinkDto(pl.getId(), pl.getLabel(), pl.getUrl()))
                .toList();

        PostSummaryDto previous = postRepository
                .findFirstByStatusAndPublishedAtLessThanOrderByPublishedAtDesc(PostStatus.PUBLISHED, post.getPublishedAt())
                .map(p -> toSummaryDto(p, normalizedLang))
                .orElse(null);

        PostSummaryDto next = postRepository
                .findFirstByStatusAndPublishedAtGreaterThanOrderByPublishedAtAsc(PostStatus.PUBLISHED, post.getPublishedAt())
                .map(p -> toSummaryDto(p, normalizedLang))
                .orElse(null);

        return new PostDetailDto(
                post.getId(), post.getSlug(), translation.getTitle(), translation.getSubtitle(),
                translation.getBodyMarkdown(), post.getCoverImageUrl(), post.getReadingMinutes(),
                post.getPublishedAt(), categoryService.toDto(post.getCategory(), normalizedLang),
                mediaDtos, pressLinkDtos, previous, next, resolved.fallback());
    }

    private PostSummaryDto toSummaryDto(Post post, String lang) {
        var resolved = localizationService.resolve(post.getTranslations(), lang, PostTranslation::getLang);
        String title = resolved.map(r -> r.value().getTitle()).orElse(post.getSlug());
        String excerpt = resolved.map(r -> r.value().getExcerpt()).orElse(null);
        boolean fallback = resolved.map(LocalizationService.Resolved::fallback).orElse(false);

        return new PostSummaryDto(post.getId(), post.getSlug(), title, excerpt, post.getCoverImageUrl(),
                post.getReadingMinutes(), post.getPublishedAt(),
                categoryService.toDto(post.getCategory(), lang), fallback);
    }

    // ---------------------------------------------------------------------
    // Admin write path — TODO per "04 · Engineering Setup & Roadmap" (Phase 4).
    // Wiring below is functional but not yet exercised by tests or the admin UI.
    // ---------------------------------------------------------------------

    @Transactional
    public Long upsertDraft(PostUpsertRequest request) {
        Category category = categoryRepository.findByKey(request.categoryKey())
                .orElseThrow(() -> new BadRequestException("Unknown category: " + request.categoryKey()));

        Post post = request.id() != null
                ? postRepository.findById(request.id())
                        .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + request.id()))
                : Post.builder().status(PostStatus.DRAFT).build();

        post.setSlug(request.slug());
        post.setCategory(category);
        post.setCoverImageUrl(request.coverImageUrl());
        post.setReadingMinutes(request.readingMinutes());

        post.getTranslations().clear();
        for (PostTranslationInput t : request.translations()) {
            post.getTranslations().add(PostTranslation.builder()
                    .post(post)
                    .lang(t.lang())
                    .title(t.title())
                    .subtitle(t.subtitle())
                    .excerpt(t.excerpt())
                    .bodyMarkdown(t.bodyMarkdown())
                    .build());
        }

        return postRepository.save(post).getId();
    }

    @Transactional
    public void publish(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        boolean hasRequiredLang = post.getTranslations().stream()
                .anyMatch(t -> t.getLang().equals(localizationService.defaultLang()));
        if (!hasRequiredLang) {
            throw new BadRequestException("Cannot publish without a " + localizationService.defaultLang() + " translation");
        }

        post.setStatus(PostStatus.PUBLISHED);
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
        postRepository.save(post);
    }
}
