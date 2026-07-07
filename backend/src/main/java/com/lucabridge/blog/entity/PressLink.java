package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "press_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PressLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
