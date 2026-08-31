package com.lucabridge.core.event;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.content.PublishableContent;
import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * No {@code service_id} here, unlike {@link com.lucabridge.core.blog.Blog} — the schema was
 * never given one. The 6a/6c home mockups show a service/category chip on event cards; there is
 * currently nowhere in the DB to source it from. Flagged for the human, not silently invented.
 */
@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Event extends PublishableContent {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_media_id")
    private Media coverMedia;

    @Enumerated(EnumType.STRING)
    @Column(name = "gallery_layout", nullable = false, length = 20)
    @Builder.Default
    private GalleryLayout galleryLayout = GalleryLayout.NONE;

    /** When the event HAPPENS. Never confuse with the publish window (inherited) or the registration window below. */
    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "venue_map_url", length = 1000)
    private String venueMapUrl;

    @Column(name = "is_registerable", nullable = false)
    @Builder.Default
    private boolean registerable = false;

    @Column(name = "registration_opens_at")
    private Instant registrationOpensAt;

    @Column(name = "registration_closes_at")
    private Instant registrationClosesAt;

    /** null = unlimited; over capacity means new submissions land on the waitlist. */
    @Column
    private Integer capacity;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EventText text;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<EventGallery> gallery = new ArrayList<>();
}
