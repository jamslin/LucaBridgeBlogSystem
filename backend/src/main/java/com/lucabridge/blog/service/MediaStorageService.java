package com.lucabridge.blog.service;

import com.lucabridge.blog.config.AppProperties;
import com.lucabridge.blog.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.UUID;

/**
 * Uploads to the S3-compatible bucket (MinIO locally, any S3-compatible store in prod
 * — see S3ClientConfig). Only image types are accepted; size is capped by
 * spring.servlet.multipart in application.yml.
 */
@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif");

    private final S3Client s3Client;
    private final AppProperties appProperties;

    public MediaStorageService(S3Client s3Client, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.appProperties = appProperties;
    }

    public String upload(MultipartFile file) {
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

        return appProperties.getStorage().getPublicBaseUrl() + "/" + bucket + "/" + key;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
