package com.lucabridge.core.blog;

import com.lucabridge.core.blog.dto.BlogUpsertRequest;
import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.MediaRepository;
import com.lucabridge.core.publish.PublishStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final MediaRepository mediaRepository;

    public BlogService(BlogRepository blogRepository, MediaRepository mediaRepository) {
        this.blogRepository = blogRepository;
        this.mediaRepository = mediaRepository;
    }

    // ---- public ----

    @Transactional(readOnly = true)
    public Page<Blog> listPublished(Pageable pageable) {
        return blogRepository.findVisible(Instant.now(), pageable);
    }

    @Transactional(readOnly = true)
    public Blog getPublishedBySlug(String slug) {
        return blogRepository.findVisibleBySlug(slug, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + slug));
    }

    /** Older neighbour of a post, or null when it is the oldest visible one. */
    @Transactional(readOnly = true)
    public Blog findOlder(Instant publishedAt) {
        if (publishedAt == null) {
            return null;
        }
        List<Blog> found = blogRepository.findOlderThan(publishedAt, Instant.now(), PageRequest.of(0, 1));
        return found.isEmpty() ? null : found.get(0);
    }

    /** Newer neighbour of a post, or null when it is the newest visible one. */
    @Transactional(readOnly = true)
    public Blog findNewer(Instant publishedAt) {
        if (publishedAt == null) {
            return null;
        }
        List<Blog> found = blogRepository.findNewerThan(publishedAt, Instant.now(), PageRequest.of(0, 1));
        return found.isEmpty() ? null : found.get(0);
    }

    // ---- admin ----

    @Transactional(readOnly = true)
    public Page<Blog> listActive(Pageable pageable) {
        return blogRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public Blog getActiveById(Long id) {
        return blogRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + id));
    }

    @Transactional
    public Blog create(BlogUpsertRequest req, Long currentUserId) {
        Blog blog = Blog.builder()
                .slug(req.slug())
                .serviceId(req.serviceId())
                .authorId(req.authorId())
                .readMinutes(req.readMinutes())
                .galleryLayout(req.galleryLayout() == null ? GalleryLayout.NONE : req.galleryLayout())
                .status(req.status())
                .publishAt(req.publishAt())
                .unpublishAt(req.unpublishAt())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        applyCoverMedia(blog, req.coverMediaId());
        applyText(blog, req);
        applyGallery(blog, req.galleryMediaIds());
        applyFirstPublish(blog);
        return save(blog);
    }

    @Transactional
    public Blog update(Long id, BlogUpsertRequest req, Long currentUserId) {
        Blog blog = blogRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + id));
        blog.setSlug(req.slug());
        blog.setServiceId(req.serviceId());
        blog.setAuthorId(req.authorId());
        blog.setReadMinutes(req.readMinutes());
        blog.setGalleryLayout(req.galleryLayout() == null ? GalleryLayout.NONE : req.galleryLayout());
        blog.setStatus(req.status());
        blog.setPublishAt(req.publishAt());
        blog.setUnpublishAt(req.unpublishAt());
        blog.setUpdatedBy(currentUserId);
        applyCoverMedia(blog, req.coverMediaId());
        applyText(blog, req);
        applyGallery(blog, req.galleryMediaIds());
        applyFirstPublish(blog);
        return save(blog);
    }

    /** Soft delete only: sets deleted_at. Never hard-deleted — media_usage depends on it still counting. */
    @Transactional
    public void softDelete(Long id, Long currentUserId) {
        Blog blog = blogRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + id));
        blog.setDeletedAt(Instant.now());
        blog.setUpdatedBy(currentUserId);
    }

    private void applyCoverMedia(Blog blog, Long coverMediaId) {
        if (coverMediaId == null) {
            blog.setCoverMedia(null);
            return;
        }
        Media media = mediaRepository.findById(coverMediaId)
                .orElseThrow(() -> new BadRequestException("Unknown media: " + coverMediaId));
        blog.setCoverMedia(media);
    }

    private void applyText(Blog blog, BlogUpsertRequest req) {
        BlogText text = blog.getText();
        if (text == null) {
            text = new BlogText();
            blog.setText(text);
            text.setBlog(blog);
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
    }

    /** Clears and rebuilds the collection rather than diffing — orphanRemoval handles the deletes. */
    private void applyGallery(Blog blog, List<Long> mediaIds) {
        blog.getGallery().clear();
        if (mediaIds == null) {
            return;
        }
        int order = 0;
        for (Long mediaId : mediaIds) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new BadRequestException("Unknown media: " + mediaId));
            blog.getGallery().add(BlogGallery.builder()
                    .blog(blog)
                    .media(media)
                    .sortOrder(order++)
                    .build());
        }
    }

    /** published_at is set once, on first publish, and never moved by later edits. */
    private void applyFirstPublish(Blog blog) {
        if (blog.getStatus() == PublishStatus.PUBLISHED && blog.getPublishedAt() == null) {
            blog.setPublishedAt(Instant.now());
        }
    }

    /**
     * Forces the flush so a slug conflict surfaces here as a clean ConflictException rather
     * than as a DataIntegrityViolationException at some unrelated later commit — the same
     * deferred-write trap MediaService.delete() had.
     */
    private Blog save(Blog blog) {
        try {
            Blog saved = blogRepository.save(blog);
            blogRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Slug already in use: " + blog.getSlug());
        }
    }
}
