package com.lucabridge.core.blog;

import com.lucabridge.core.blog.dto.AdminBlogDetailDto;
import com.lucabridge.core.blog.dto.AdminBlogSummaryDto;
import com.lucabridge.core.blog.dto.BlogDetailDto;
import com.lucabridge.core.blog.dto.BlogSummaryDto;
import com.lucabridge.core.blog.dto.GalleryImageDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.dto.MediaRefDto;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;
import java.util.List;

/**
 * Entity -> DTO only. Every translated field resolves through {@link Localized#pick}; nothing
 * here re-derives a fallback with a null check or a COALESCE, on purpose — see the class doc on
 * {@link Localized}.
 */
final class BlogMapper {

    private BlogMapper() {
    }

    static BlogSummaryDto toSummary(Blog blog, Lang lang) {
        BlogText t = blog.getText();
        Media cover = blog.getCoverMedia();
        return new BlogSummaryDto(
                blog.getId(),
                blog.getSlug(),
                title(t, lang),
                summary(t, lang),
                cover == null ? null : cover.getUrl(),
                cover == null ? null : cover.getWidth(),
                cover == null ? null : cover.getHeight(),
                blog.getPublishedAt(),
                blog.getReadMinutes());
    }

    static BlogDetailDto toDetail(Blog blog, Lang lang) {
        BlogText t = blog.getText();
        Media cover = blog.getCoverMedia();
        List<GalleryImageDto> gallery = blog.getGallery().stream()
                .map(g -> new GalleryImageDto(g.getMedia().getUrl(), g.getMedia().getWidth(), g.getMedia().getHeight()))
                .toList();
        return new BlogDetailDto(
                blog.getId(),
                blog.getSlug(),
                title(t, lang),
                summary(t, lang),
                t == null ? null : Localized.pick(lang, t.getTcBody(), t.getEnBody(), t.getScBody()),
                cover == null ? null : cover.getUrl(),
                cover == null ? null : cover.getWidth(),
                cover == null ? null : cover.getHeight(),
                blog.getPublishedAt(),
                blog.getReadMinutes(),
                blog.getGalleryLayout(),
                gallery);
    }

    static AdminBlogSummaryDto toAdminSummary(Blog blog, Instant now) {
        BlogText t = blog.getText();
        return new AdminBlogSummaryDto(
                blog.getId(),
                blog.getSlug(),
                t == null ? null : t.getTcTitle(),
                blog.getStatus(),
                stateOf(blog, now),
                blog.getPublishAt(),
                blog.getUnpublishAt(),
                blog.getPublishedAt(),
                blog.getUpdatedAt());
    }

    static AdminBlogDetailDto toAdminDetail(Blog blog, Instant now) {
        BlogText t = blog.getText();
        Media cover = blog.getCoverMedia();
        List<Long> galleryIds = blog.getGallery().stream().map(g -> g.getMedia().getId()).toList();
        List<MediaRefDto> galleryRefs = blog.getGallery().stream()
                .map(g -> new MediaRefDto(g.getMedia().getId(), g.getMedia().getUrl()))
                .toList();
        return new AdminBlogDetailDto(
                blog.getId(),
                blog.getSlug(),
                blog.getServiceId(),
                cover == null ? null : cover.getId(),
                blog.getAuthorId(),
                blog.getReadMinutes(),
                blog.getGalleryLayout(),
                blog.getStatus(),
                stateOf(blog, now),
                blog.getPublishAt(),
                blog.getUnpublishAt(),
                blog.getPublishedAt(),
                t == null ? null : t.getTcTitle(),
                t == null ? null : t.getEnTitle(),
                t == null ? null : t.getScTitle(),
                t == null ? null : t.getTcSummary(),
                t == null ? null : t.getEnSummary(),
                t == null ? null : t.getScSummary(),
                t == null ? null : t.getTcBody(),
                t == null ? null : t.getEnBody(),
                t == null ? null : t.getScBody(),
                galleryIds,
                galleryRefs);
    }

    private static String title(BlogText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcTitle(), t.getEnTitle(), t.getScTitle());
    }

    private static String summary(BlogText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcSummary(), t.getEnSummary(), t.getScSummary());
    }

    private static Visibility.State stateOf(Blog blog, Instant now) {
        return Visibility.stateOf(blog.getStatus(), blog.getPublishAt(), blog.getUnpublishAt(),
                blog.getDeletedAt(), now);
    }
}
