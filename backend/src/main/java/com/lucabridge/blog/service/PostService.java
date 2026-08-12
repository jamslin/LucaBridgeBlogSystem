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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

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
    // Admin read path — powers the CMS posts list and editor (all statuses,
    // raw translations with no language fallback).
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AdminPostSummaryDto> listAllForAdmin() {
        return postRepository.findAll().stream()
                .sorted(Comparator.comparing(Post::getId).reversed())
                .map(this::toAdminSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPostDetailDto getForEdit(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        List<PostTranslationInput> translations = post.getTranslations().stream()
                .map(t -> new PostTranslationInput(t.getLang(), t.getTitle(), t.getSubtitle(),
                        t.getExcerpt(), t.getBodyMarkdown()))
                .toList();

        return new AdminPostDetailDto(post.getId(), post.getSlug(),
                post.getCategory() != null ? post.getCategory().getKey() : null,
                post.getCoverImageUrl(), post.getReadingMinutes(),
                post.getStatus().name(), post.getPublishedAt(), translations);
    }

    private AdminPostSummaryDto toAdminSummary(Post post) {
        String defaultLang = localizationService.defaultLang();
        String title = post.getTranslations().stream()
                .filter(t -> defaultLang.equals(t.getLang()))
                .map(PostTranslation::getTitle)
                .findFirst()
                .orElseGet(() -> post.getTranslations().stream()
                        .map(PostTranslation::getTitle).findFirst().orElse(post.getSlug()));
        return new AdminPostSummaryDto(post.getId(), post.getSlug(), post.getStatus().name(),
                post.getCategory() != null ? post.getCategory().getKey() : null, title, post.getPublishedAt());
    }

    // ---------------------------------------------------------------------
    // Admin write path.
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

        // Reconcile translations by lang IN PLACE. Clearing then re-adding the same
        // (post_id, lang) rows makes Hibernate flush the new INSERTs before the orphan
        // DELETEs, violating the (post_id, lang) unique constraint when editing a post.
        Map<String, PostTranslation> existing = new HashMap<>();
        for (PostTranslation t : post.getTranslations()) {
            existing.put(t.getLang(), t);
        }
        Set<String> incomingLangs = new HashSet<>();
        for (PostTranslationInput in : request.translations()) {
            incomingLangs.add(in.lang());
            PostTranslation t = existing.get(in.lang());
            if (t == null) {
                t = PostTranslation.builder().post(post).lang(in.lang()).build();
                post.getTranslations().add(t);
            }
            t.setTitle(in.title());
            t.setSubtitle(in.subtitle());
            t.setExcerpt(in.excerpt());
            t.setBodyMarkdown(in.bodyMarkdown());
        }
        post.getTranslations().removeIf(t -> !incomingLangs.contains(t.getLang()));

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

    @Transactional
    public void unpublish(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setStatus(PostStatus.DRAFT);
        postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        postRepository.delete(post);
    }
}
