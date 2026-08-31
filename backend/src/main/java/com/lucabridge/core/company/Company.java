package com.lucabridge.core.company;

import com.lucabridge.core.media.Media;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/** Single row, id = 1 always (DB CHECK (id = 1)). Seeded by V2__reference_data.sql — never created or deleted through the app. */
@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    private Short id;

    @Column(name = "charity_reg_no", length = 40)
    private String charityRegNo;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(length = 40)
    private String phone;

    @Column(length = 320)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_media_id")
    private Media logoMedia;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CompanyText text;
}
