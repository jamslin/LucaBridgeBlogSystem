package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "lang"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 10)
    private String lang;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 400)
    private String subtitle;

    @Column(length = 600)
    private String excerpt;

    @Column(name = "body_markdown", nullable = false, columnDefinition = "text")
    private String bodyMarkdown;
}
