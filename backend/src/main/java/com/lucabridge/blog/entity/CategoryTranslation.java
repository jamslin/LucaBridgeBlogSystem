package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "lang"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 10)
    private String lang;

    @Column(nullable = false, length = 120)
    private String name;
}
