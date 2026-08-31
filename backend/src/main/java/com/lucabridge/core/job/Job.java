package com.lucabridge.core.job;

import com.lucabridge.core.content.PublishableContent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/** No cover, no gallery, no service_id — the design doesn't use any of them here. */
@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Job extends PublishableContent {

    @Column(name = "employment_type", length = 30)
    private String employmentType;

    @Column(length = 120)
    private String department;

    @Column(name = "posted_at")
    private Instant postedAt;

    /** Application deadline — folds into Visibility.JPQL_JOB, not just the publish window. */
    @Column(name = "closes_at")
    private Instant closesAt;

    @Column(name = "apply_email", length = 320)
    private String applyEmail;

    @Column(name = "apply_url", length = 1000)
    private String applyUrl;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private JobText text;
}
