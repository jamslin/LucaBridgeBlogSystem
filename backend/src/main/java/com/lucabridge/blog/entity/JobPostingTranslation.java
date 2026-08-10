package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_posting_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"job_posting_id", "lang"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false, length = 10)
    private String lang;

    @Column(nullable = false, length = 300)
    private String title;

    /** Human-readable employment-type label, e.g. 全職 / Full-time. */
    @Column(name = "employment_type_label", length = 60)
    private String employmentTypeLabel;

    @Column(length = 600)
    private String summary;

    @Column(name = "body_markdown", nullable = false, columnDefinition = "text")
    private String bodyMarkdown;
}
