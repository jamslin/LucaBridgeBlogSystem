package com.lucabridge.core.media;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * One catalogued image. Every reference to this row from blog/event covers, galleries, home
 * blocks, service icons or the company logo is a foreign key with {@code ON DELETE RESTRICT} —
 * see the {@code media_usage} view. There is no bare-URL storage anywhere in the schema.
 */
@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, unique = true, length = 500)
    private String s3Key;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "file_name", length = 300)
    private String fileName;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "byte_size")
    private Long byteSize;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(length = 64)
    private String checksum;

    /** Id of the uploading app_user. Plain FK, not a relation — the catalogue never needs to join it. */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MediaText text;
}
