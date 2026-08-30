package com.lucabridge.core.media;

import jakarta.persistence.*;
import lombok.*;

/**
 * Alt text and caption for a {@link Media} row, one language per column. Every field is
 * nullable by design — a decorative image legitimately has no alt text — so, unlike
 * blog/event/job's text side tables, this row is only created once someone actually sets one
 * of these fields, not automatically alongside the upload.
 */
@Entity
@Table(name = "media_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaText {

    @Id
    @Column(name = "media_id")
    private Long mediaId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "tc_alt", length = 300)
    private String tcAlt;

    @Column(name = "en_alt", length = 300)
    private String enAlt;

    @Column(name = "sc_alt", length = 300)
    private String scAlt;

    @Column(name = "tc_caption", length = 500)
    private String tcCaption;

    @Column(name = "en_caption", length = 500)
    private String enCaption;

    @Column(name = "sc_caption", length = 500)
    private String scCaption;
}
