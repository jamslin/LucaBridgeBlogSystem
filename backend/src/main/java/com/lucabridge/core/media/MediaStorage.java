package com.lucabridge.core.media;

import com.lucabridge.core.config.AppProperties;
import com.lucabridge.core.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Stores/removes/lists objects in the S3-compatible bucket (MinIO locally). Only image types
 * are accepted on upload; size is capped by spring.servlet.multipart. Cataloguing lives in
 * {@link MediaService} — this class only touches storage.
 */
@Service
public class MediaStorage {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif");

    private final S3Client s3Client;
    private final AppProperties appProperties;

    public MediaStorage(S3Client s3Client, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.appProperties = appProperties;
    }

    public StoredMedia store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported file type: " + contentType
                    + " (allowed: JPEG, PNG, WebP, GIF, AVIF)");
        }
        if (file.isEmpty()) {
            throw new BadRequestException("Empty file");
        }

        String bucket = appProperties.getStorage().getBucket();
        String key = UUID.randomUUID() + "-" + sanitizeFilename(file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload media", e);
        }

        return new StoredMedia(key, urlFor(bucket, key), contentType, file.getSize());
    }

    public void delete(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(appProperties.getStorage().getBucket())
                .key(objectKey)
                .build());
    }

    /** Every object currently in the bucket — used to backfill the catalogue. */
    public List<StoredObject> listAll() {
        String bucket = appProperties.getStorage().getBucket();
        List<StoredObject> out = new ArrayList<>();
        s3Client.listObjectsV2Paginator(ListObjectsV2Request.builder().bucket(bucket).build())
                .contents()
                .forEach(o -> out.add(new StoredObject(o.key(), urlFor(bucket, o.key()), o.size())));
        return out;
    }

    private String urlFor(String bucket, String key) {
        return appProperties.getStorage().getPublicBaseUrl() + "/" + bucket + "/" + key;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredMedia(String objectKey, String url, String contentType, long sizeBytes) {}

    public record StoredObject(String objectKey, String url, long sizeBytes) {}
}
