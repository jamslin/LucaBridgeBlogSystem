package com.lucabridge.core.homeblock;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "home_block_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeBlockText {

    @Id
    @Column(name = "home_block_id")
    private Long homeBlockId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "home_block_id")
    private HomeBlock homeBlock;

    @Column(name = "tc_title", nullable = false, length = 300)
    private String tcTitle;

    @Column(name = "en_title", length = 300)
    private String enTitle;

    @Column(name = "sc_title", length = 300)
    private String scTitle;

    @Column(name = "tc_subtitle", length = 600)
    private String tcSubtitle;

    @Column(name = "en_subtitle", length = 600)
    private String enSubtitle;

    @Column(name = "sc_subtitle", length = 600)
    private String scSubtitle;

    @Column(name = "tc_eyebrow", length = 120)
    private String tcEyebrow;

    @Column(name = "en_eyebrow", length = 120)
    private String enEyebrow;

    @Column(name = "sc_eyebrow", length = 120)
    private String scEyebrow;

    /** Fine print beside the CTA. A newline renders as a second line. */
    @Column(name = "tc_note", length = 300)
    private String tcNote;

    @Column(name = "en_note", length = 300)
    private String enNote;

    @Column(name = "sc_note", length = 300)
    private String scNote;

    @Column(name = "tc_button_label", length = 100)
    private String tcButtonLabel;

    @Column(name = "en_button_label", length = 100)
    private String enButtonLabel;

    @Column(name = "sc_button_label", length = 100)
    private String scButtonLabel;
}
