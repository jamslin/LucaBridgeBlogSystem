package com.lucabridge.core.job;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobText {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(name = "tc_title", nullable = false, length = 300)
    private String tcTitle;

    @Column(name = "en_title", length = 300)
    private String enTitle;

    @Column(name = "sc_title", length = 300)
    private String scTitle;

    @Column(name = "tc_body", columnDefinition = "text")
    private String tcBody;

    @Column(name = "en_body", columnDefinition = "text")
    private String enBody;

    @Column(name = "sc_body", columnDefinition = "text")
    private String scBody;

    @Column(name = "tc_location", length = 300)
    private String tcLocation;

    @Column(name = "en_location", length = 300)
    private String enLocation;

    @Column(name = "sc_location", length = 300)
    private String scLocation;
}
