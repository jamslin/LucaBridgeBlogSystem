package com.lucabridge.core.content;

import com.lucabridge.core.publish.PublishStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * The columns identical on {@code blog}, {@code event} and {@code job} — the publish contract
 * (see the schema header) plus {@code slug} and the audit/soft-delete columns. Deliberately NOT
 * a shared table with a type discriminator: that was considered and rejected, so this exists
 * only to avoid re-typing the same eleven columns three times, not to merge the entities.
 *
 * <p>Soft delete only: {@code deletedAt} is set, never cleared by hard-deleting the row. The
 * media reference count in {@code media_usage} depends on soft-deleted rows still counting.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class PublishableContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PublishStatus status = PublishStatus.DRAFT;

    @Column(name = "publish_at")
    private Instant publishAt;

    @Column(name = "unpublish_at")
    private Instant unpublishAt;

    /** First publish; display date and sort key. Set once, never cleared or moved. */
    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
