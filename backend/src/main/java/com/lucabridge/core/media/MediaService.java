package com.lucabridge.core.media;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Uploads, reference-counted deletes and sweeps the media catalogue. Storage (S3/MinIO) is
 * {@link MediaStorage}'s job; this class owns the {@code media} row and the delete-ordering
 * rules that keep the two in sync.
 */
@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;
    /**
     * Used explicitly rather than {@code @Transactional} for the delete path: {@link #sweep()}
     * needs to run {@link #deleteOne} once per item so a single bad row or object cannot roll
     * back the whole batch, and calling an {@code @Transactional} method on {@code this} from
     * inside the same class bypasses the Spring proxy and silently drops the annotation. A
     * {@link TransactionTemplate} demarcates the transaction inline, so it works the same way
     * regardless of how the method is called.
     */
    private final TransactionTemplate transactionTemplate;

    public MediaService(MediaRepository mediaRepository, MediaStorage mediaStorage,
                         PlatformTransactionManager transactionManager) {
        this.mediaRepository = mediaRepository;
        this.mediaStorage = mediaStorage;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public List<Media> list() {
        return mediaRepository.findAllWithText();
    }

    @Transactional(readOnly = true)
    public long usageCount(Long id) {
        return mediaRepository.countUsage(id);
    }

    @Transactional(readOnly = true)
    public Media get(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + id));
    }

    /** Single-language alt text for now — media_text carries tc/en/sc, but the admin UI only exposes 繁中 today. */
    @Transactional
    public Media updateAltText(Long id, String altText) {
        Media media = get(id);
        MediaText text = media.getText();
        if (text == null) {
            text = new MediaText();
            text.setMedia(media);
            media.setText(text);
        }
        text.setTcAlt(altText);
        return media;
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
        readDimensions(file).ifPresent(dim -> {
            media.setWidth(dim.width());
            media.setHeight(dim.height());
        });
        return mediaRepository.save(media);
    }

    /**
     * Refuses if {@code media_usage} still has a row for this image. On zero usage, deletes the
     * DB row first and, only once that transaction has actually committed, the S3 object — a
     * failure between the two steps then leaves an invisible orphan the sweeper cleans up later,
     * rather than a live page with a broken image (the reverse ordering). The pre-check and the
     * commit happen inside one transaction, so the object is never removed before the row-delete
     * is durable.
     */
    public void delete(Long mediaId) {
        String s3Key = deleteOne(mediaId);
        mediaStorage.delete(s3Key);
    }

    /**
     * Deletes the row in its own transaction and returns its {@code s3_key}. Also the backstop
     * for the TOCTOU window between the {@code countUsage} check and the delete: if a reference
     * is added in between, the {@code ON DELETE RESTRICT} foreign key rejects the delete and
     * Hibernate surfaces it as {@link DataIntegrityViolationException} on the forced
     * {@link MediaRepository#flush()} below — translated here to the same
     * {@link ConflictException} the explicit check throws, so both paths look identical to the
     * caller instead of one of them being a raw 500.
     */
    private String deleteOne(Long mediaId) {
        return transactionTemplate.execute(status -> {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));
            long usageCount = mediaRepository.countUsage(mediaId);
            if (usageCount > 0) {
                throw new ConflictException(
                        "Media " + mediaId + " is still referenced by " + usageCount + " item(s)");
            }
            try {
                mediaRepository.delete(media);
                mediaRepository.flush();
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("Media " + mediaId + " is still referenced by another item");
            }
            return media.getS3Key();
        });
    }

    /**
     * What {@link #sweep()} would delete, without deleting anything. The CMS must show this and
     * get explicit confirmation before calling {@link #sweep()} — it is the only irreversible
     * button in the CMS.
     */
    @Transactional(readOnly = true)
    public SweepOutcome previewSweep() {
        List<Media> unusedRows = mediaRepository.findUnused();
        Set<String> cataloguedKeys = new HashSet<>(mediaRepository.findAllS3Keys());
        List<String> orphanObjectKeys = mediaStorage.listAll().stream()
                .map(MediaStorage.StoredObject::objectKey)
                .filter(key -> !cataloguedKeys.contains(key))
                .toList();
        return new SweepOutcome(unusedRows, orphanObjectKeys);
    }

    /**
     * Two-pass sweep: DB rows in {@code media_unused} first, then any S3 object with no matching
     * catalogue row. Each row (and each orphan object) is removed as its own step via
     * {@link #deleteOne}/{@link MediaStorage#delete}, not one transaction spanning the whole
     * batch, so a single bad row or object cannot undo everything else the sweep already
     * removed. A failure is logged and skipped rather than aborting the sweep. Callers must have
     * shown {@link #previewSweep()} to the admin first.
     */
    public SweepOutcome sweep() {
        SweepOutcome preview = previewSweep();

        List<Media> removedRows = new ArrayList<>();
        for (Media media : preview.unusedRows()) {
            try {
                delete(media.getId());
                removedRows.add(media);
            } catch (RuntimeException e) {
                log.warn("Sweep: skipping media {} — {}", media.getId(), e.getMessage());
            }
        }

        List<String> removedOrphans = new ArrayList<>();
        for (String key : preview.orphanObjectKeys()) {
            try {
                mediaStorage.delete(key);
                removedOrphans.add(key);
            } catch (RuntimeException e) {
                log.warn("Sweep: failed to remove orphan object {} — {}", key, e.getMessage());
            }
        }

        return new SweepOutcome(removedRows, removedOrphans);
    }

    /** Best-effort; an unreadable or unsupported format (e.g. AVIF has no default ImageIO plugin) just leaves width/height null. */
    private Optional<Dimensions> readDimensions(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            return image == null
                    ? Optional.empty()
                    : Optional.of(new Dimensions(image.getWidth(), image.getHeight()));
        } catch (IOException e) {
            log.warn("Could not read image dimensions for {}: {}", file.getOriginalFilename(), e.getMessage());
            return Optional.empty();
        }
    }

    private record Dimensions(int width, int height) {
    }

    public record SweepOutcome(List<Media> unusedRows, List<String> orphanObjectKeys) {
    }
}
