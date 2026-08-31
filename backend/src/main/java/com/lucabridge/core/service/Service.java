package com.lucabridge.core.service;

import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.*;

/**
 * One taxonomy shared by blog tagging, event tagging, the home chip row, the service cards and
 * the services page. No publish window or soft delete — it's a reference table, not content;
 * {@code is_active} is the only visibility gate. Named to match the table exactly, same as
 * every other entity in this codebase — the resulting collision with Spring's own
 * {@code @Service} stereotype is real but contained to {@link ServiceService}, which resolves
 * it with a fully-qualified annotation.
 */
@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_media_id")
    private Media iconMedia;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToOne(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ServiceText text;
}
