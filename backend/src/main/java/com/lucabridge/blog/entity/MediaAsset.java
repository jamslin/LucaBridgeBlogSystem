package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A catalogued uploaded image (the Media Library). Standalone — NOT tied to a post,
 * unlike {@link Media} (which is a post's inline gallery). One row is created on every
 * upload; objectKey lets us delete the underlying MinIO object.
 */
@Entity
@Table(name = "media_asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_key", nullable = false, unique = true, length = 400)
    private String objectKey;

    @Column(nullable = false, length = 700)
    private String url;

    @Column(length = 300)
    private String filename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "alt_text", length = 500)
    private String altText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
