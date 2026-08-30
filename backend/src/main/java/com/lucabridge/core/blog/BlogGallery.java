package com.lucabridge.core.blog;

import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blog_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogGallery {

    @EmbeddedId
    @Builder.Default
    private BlogGalleryId id = new BlogGalleryId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id")
    private Blog blog;

    /** RESTRICT at the DB, not CASCADE: an image in a gallery can't be silently unlinked by deleting it. */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
