package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "homepage_banner")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HomepageBanner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "image_url", nullable = false, length = 500) private String imageUrl;
    @Column(name = "link_url", length = 500) private String linkUrl;
    @Column(name = "sort_order", nullable = false) @Builder.Default private int sortOrder = 0;
    @Column(nullable = false) @Builder.Default private boolean active = true;
    @Column(name = "starts_at") private Instant startsAt;
    @Column(name = "ends_at") private Instant endsAt;
    @Column(name = "title_zh_hant", nullable = false, length = 250) private String titleZhHant;
    @Column(name = "subtitle_zh_hant", length = 500) private String subtitleZhHant;
    @Column(name = "button_label_zh_hant", length = 100) private String buttonLabelZhHant;
    @Column(name = "title_en", length = 250) private String titleEn;
    @Column(name = "subtitle_en", length = 500) private String subtitleEn;
    @Column(name = "button_label_en", length = 100) private String buttonLabelEn;
    @Column(name = "title_zh_hans", length = 250) private String titleZhHans;
    @Column(name = "subtitle_zh_hans", length = 500) private String subtitleZhHans;
    @Column(name = "button_label_zh_hans", length = 100) private String buttonLabelZhHans;
}
