package com.lucabridge.core.event;

import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventGallery {

    @EmbeddedId
    @Builder.Default
    private EventGalleryId id = new EventGalleryId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    /** RESTRICT at the DB, not CASCADE: an image in a gallery can't be silently unlinked by deleting it. */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
