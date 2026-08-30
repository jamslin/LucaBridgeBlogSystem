package com.lucabridge.core.media;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uploads, reference-counted deletes and sweeps the media catalogue. Storage (S3/MinIO) is
 * {@link MediaStorage}'s job; this class owns the {@code media} row and the delete-ordering
 * rules that keep the two in sync.
 */
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;

    public MediaService(MediaRepository mediaRepository, MediaStorage mediaStorage) {
        this.mediaRepository = mediaRepository;
        this.mediaStorage = mediaStorage;
    }

    @Transactional
    public Media upload(MultipartFile file, Long uploadedByUserId) {
        MediaStorage.StoredMedia stored = mediaStorage.store(file);
        Media media = Media.builder()
                .s3Key(stored.objectKey())
                .url(stored.url())
                .fileName(file.getOriginalFilename())
                .mimeType(stored.contentType())
                .byteSize(stored.sizeBytes())
                .uploadedBy(uploadedByUserId)
                .build();
        return mediaRepository.save(media);
    }

    /**
     * Refuses if {@code media_usage} still has a row for this image. On zero usage, deletes the
     * DB row first and only then the S3 object: a failure between the two steps leaves an
     * invisible orphan the sweeper can clean up later, rather than a live page with a broken
     * image (the reverse ordering).
     */
    @Transactional
    public void delete(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));
        long usageCount = mediaRepository.countUsage(mediaId);
        if (usageCount > 0) {
            throw new ConflictException(
                    "Media " + mediaId + " is still referenced by " + usageCount + " item(s)");
        }
        mediaRepository.delete(media);
        mediaStorage.delete(media.getS3Key());
    }

    /**
     * What {@link #sweep()} would delete, without deleting anything. The CMS must show this and
     * get explicit confirmation before calling {@link #sweep()} — it is the only irreversible
     * button in the CMS.
     */
    @Transactional(readOnly = true)
    public SweepOutcome previewSweep() {
        List<Media> unusedRows = mediaRepository.findUnused();
        Set<String> cataloguedKeys = mediaRepository.findAll().stream()
                .map(Media::getS3Key)
                .collect(Collectors.toSet());
        List<String> orphanObjectKeys = mediaStorage.listAll().stream()
                .map(MediaStorage.StoredObject::objectKey)
                .filter(key -> !cataloguedKeys.contains(key))
                .toList();
        return new SweepOutcome(unusedRows, orphanObjectKeys);
    }

    /**
     * Two-pass sweep: DB rows in {@code media_unused} first (DB row then S3 object, same
     * ordering as {@link #delete}), then any S3 object with no matching catalogue row at all.
     * Callers must have shown {@link #previewSweep()} to the admin first.
     */
    @Transactional
    public SweepOutcome sweep() {
        SweepOutcome outcome = previewSweep();
        for (Media media : outcome.unusedRows()) {
            mediaRepository.delete(media);
            mediaStorage.delete(media.getS3Key());
        }
        outcome.orphanObjectKeys().forEach(mediaStorage::delete);
        return outcome;
    }

    public record SweepOutcome(List<Media> unusedRows, List<String> orphanObjectKeys) {
    }
}
