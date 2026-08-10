package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 職位空缺 — a job vacancy. Modelled on {@link Event}: a slug + status + a list of
 * per-language translations, plus recruitment-specific fields. Kept deliberately
 * simple (read-only public listing) to match the site's lean CMS pattern.
 */
@Entity
@Table(name = "job_posting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PUBLISHED";

    /** Free-form key such as FULL_TIME / PART_TIME / CONTRACT / VOLUNTEER; label comes from the translation. */
    @Column(name = "employment_type", length = 30)
    private String employmentType;

    @Column(length = 120)
    private String department;

    @Column(name = "location_text", length = 200)
    private String locationText;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "closes_at")
    private Instant closesAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz not null default now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz not null default now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPostingTranslation> translations = new ArrayList<>();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
