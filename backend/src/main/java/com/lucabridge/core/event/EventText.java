package com.lucabridge.core.event;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventText {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "tc_title", nullable = false, length = 300)
    private String tcTitle;

    @Column(name = "en_title", length = 300)
    private String enTitle;

    @Column(name = "sc_title", length = 300)
    private String scTitle;

    @Column(name = "tc_summary", length = 600)
    private String tcSummary;

    @Column(name = "en_summary", length = 600)
    private String enSummary;

    @Column(name = "sc_summary", length = 600)
    private String scSummary;

    @Column(name = "tc_body", columnDefinition = "text")
    private String tcBody;

    @Column(name = "en_body", columnDefinition = "text")
    private String enBody;

    @Column(name = "sc_body", columnDefinition = "text")
    private String scBody;

    @Column(name = "tc_venue", length = 300)
    private String tcVenue;

    @Column(name = "en_venue", length = 300)
    private String enVenue;

    @Column(name = "sc_venue", length = 300)
    private String scVenue;
}
