package com.lucabridge.core.blog;

import jakarta.persistence.*;
import lombok.*;

/**
 * 1:1 side table, PK = FK. Unlike {@code media_text}, {@code tc_title} is mandatory: a blog
 * cannot exist without its 繁中 title, so this row is created together with its {@link Blog},
 * not lazily.
 */
@Entity
@Table(name = "blog_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogText {

    @Id
    @Column(name = "blog_id")
    private Long blogId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "blog_id")
    private Blog blog;

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
}
