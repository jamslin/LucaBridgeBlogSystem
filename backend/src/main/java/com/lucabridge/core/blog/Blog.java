package com.lucabridge.core.blog;

import com.lucabridge.core.content.GalleryLayout;
import com.lucabridge.core.content.PublishableContent;
import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Blog extends PublishableContent {

    @Column(name = "service_id")
    private Long serviceId;

    /**
     * A real association, not a plain FK id: the cover is needed on every card and every
     * detail view, so the repository queries {@code LEFT JOIN FETCH} it to avoid N+1 rather
     * than resolving it separately per row.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_media_id")
    private Media coverMedia;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "read_minutes")
    private Integer readMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "gallery_layout", nullable = false, length = 20)
    @Builder.Default
    private GalleryLayout galleryLayout = GalleryLayout.NONE;

    /** Owning side is {@link BlogText#getBlog()}; cascaded because the two rows are one record. */
    @OneToOne(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BlogText text;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<BlogGallery> gallery = new ArrayList<>();
}
