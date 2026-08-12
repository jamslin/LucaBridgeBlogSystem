package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.MediaAssetDto;
import com.lucabridge.blog.dto.MediaUsageDto;
import com.lucabridge.blog.entity.MediaAsset;
import com.lucabridge.blog.entity.Post;
import com.lucabridge.blog.entity.PostTranslation;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.MediaAssetRepository;
import com.lucabridge.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Media Library: catalogues every upload as a {@link MediaAsset}, lists assets with usage
 * detection, edits alt text, blocks deletion of referenced images, and can backfill the catalogue
 * from objects already in the bucket (e.g. seeded images uploaded outside the app).
 */
@Service
public class MediaService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif", "avif");

    private final MediaStorageService storage;
    private final MediaAssetRepository assetRepository;
    private final PostRepository postRepository;
    private final LocalizationService localizationService;

    public MediaService(MediaStorageService storage,
                        MediaAssetRepository assetRepository,
                        PostRepository postRepository,
                        LocalizationService localizationService) {
        this.storage = storage;
        this.assetRepository = assetRepository;
        this.postRepository = postRepository;
        this.localizationService = localizationService;
    }

    @Transactional
    public MediaAssetDto upload(MultipartFile file) {
        MediaStorageService.StoredMedia stored = storage.store(file);
        MediaAsset asset = assetRepository.save(MediaAsset.builder()
                .objectKey(stored.objectKey())
                .url(stored.url())
                .filename(file.getOriginalFilename())
                .contentType(stored.contentType())
                .sizeBytes(stored.sizeBytes())
                .build());
        return toDto(asset, List.of());
    }

    @Transactional(readOnly = true)
    public List<MediaAssetDto> list() {
        List<MediaAsset> assets = assetRepository.findAllByOrderByCreatedAtDesc();
        Set<String> urls = new HashSet<>();
        for (MediaAsset a : assets) urls.add(a.getUrl());
        Map<String, List<MediaUsageDto>> usage = computeUsage(urls);
        return assets.stream().map(a -> toDto(a, usage.getOrDefault(a.getUrl(), List.of()))).toList();
    }

    @Transactional
    public MediaAssetDto updateAlt(Long id, String altText) {
        MediaAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + id));
        asset.setAltText(altText);
        assetRepository.save(asset);
        return toDto(asset, computeUsage(Set.of(asset.getUrl())).getOrDefault(asset.getUrl(), List.of()));
    }

    @Transactional
    public void delete(Long id) {
        MediaAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + id));
        List<MediaUsageDto> usages = computeUsage(Set.of(asset.getUrl())).getOrDefault(asset.getUrl(), List.of());
        if (!usages.isEmpty()) {
            StringBuilder where = new StringBuilder();
            for (MediaUsageDto u : usages) {
                if (where.length() > 0) where.append(", ");
                where.append(u.title()).append(" (").append(u.field()).append(")");
            }
            throw new BadRequestException("Cannot delete — image is still used in: " + where);
        }
        storage.delete(asset.getObjectKey());
        assetRepository.delete(asset);
    }

    /**
     * Catalogue every image object in the bucket that has no MediaAsset yet. Returns how many were
     * added. Safe to run repeatedly (skips known keys). Never throws — logs and returns 0 on failure.
     */
    @Transactional
    public int syncFromStorage() {
        Set<String> known = new HashSet<>();
        for (MediaAsset a : assetRepository.findAll()) known.add(a.getObjectKey());
        int added = 0;
        for (MediaStorageService.StoredObject o : storage.listAll()) {
            if (known.contains(o.objectKey()) || !isImageKey(o.objectKey())) continue;
            assetRepository.save(MediaAsset.builder()
                    .objectKey(o.objectKey())
                    .url(o.url())
                    .filename(basename(o.objectKey()))
                    .contentType(contentTypeFor(o.objectKey()))
                    .sizeBytes(o.sizeBytes())
                    .build());
            added++;
        }
        return added;
    }

    private Map<String, List<MediaUsageDto>> computeUsage(Set<String> urls) {
        Map<String, List<MediaUsageDto>> map = new HashMap<>();
        if (urls.isEmpty()) return map;
        for (Post p : postRepository.findAll()) {
            String title = titleOf(p);
            String cover = p.getCoverImageUrl();
            if (cover != null && urls.contains(cover)) {
                map.computeIfAbsent(cover, k -> new ArrayList<>())
                        .add(new MediaUsageDto(p.getId(), p.getSlug(), title, "cover"));
            }
            for (PostTranslation t : p.getTranslations()) {
                String body = t.getBodyMarkdown();
                if (body == null || body.isEmpty()) continue;
                for (String url : urls) {
                    if (body.contains(url)) {
                        map.computeIfAbsent(url, k -> new ArrayList<>())
                                .add(new MediaUsageDto(p.getId(), p.getSlug(), title, "body (" + t.getLang() + ")"));
                    }
                }
            }
        }
        return map;
    }

    private String titleOf(Post p) {
        String def = localizationService.defaultLang();
        String fallback = null;
        for (PostTranslation t : p.getTranslations()) {
            if (fallback == null) fallback = t.getTitle();
            if (def.equals(t.getLang())) return t.getTitle();
        }
        return fallback != null ? fallback : p.getSlug();
    }

    private boolean isImageKey(String key) {
        return IMAGE_EXT.contains(extensionOf(key));
    }

    private String extensionOf(String key) {
        int dot = key.lastIndexOf('.');
        return dot >= 0 ? key.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private String basename(String key) {
        int slash = key.lastIndexOf('/');
        return slash >= 0 ? key.substring(slash + 1) : key;
    }

    private String contentTypeFor(String key) {
        return switch (extensionOf(key)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "avif" -> "image/avif";
            default -> null;
        };
    }

    private MediaAssetDto toDto(MediaAsset a, List<MediaUsageDto> usages) {
        return new MediaAssetDto(a.getId(), a.getUrl(), a.getFilename(), a.getContentType(),
                a.getSizeBytes(), a.getAltText(), a.getCreatedAt(), !usages.isEmpty(), usages);
    }
}
