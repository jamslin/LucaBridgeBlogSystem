package com.lucabridge.core.homeblock;

import com.lucabridge.core.blog.Blog;
import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Content only — React owns layout. If a column_width or background_colour column is ever
 * requested, that is the moment this becomes an accidental page builder; refuse it.
 *
 * <p>No status/deleted_at, unlike blog/event/job — visibility here is is_active plus the publish
 * window only, so this does NOT extend PublishableContent. See Visibility.JPQL_ACTIVE.
 */
@Entity
@Table(name = "home_block")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HomeBlockSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    /** FEATURED pins a story. A real relation, not a plain FK id — needed together with the block whenever FEATURED renders. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @Column(name = "link_url", length = 1000)
    private String linkUrl;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "publish_at")
    private Instant publishAt;

    @Column(name = "unpublish_at")
    private Instant unpublishAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "homeBlock", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private HomeBlockText text;
}
