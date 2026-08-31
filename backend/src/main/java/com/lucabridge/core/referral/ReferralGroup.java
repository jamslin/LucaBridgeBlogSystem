package com.lucabridge.core.referral;

import jakarta.persistence.*;
import lombok.*;

/**
 * "How did you hear about us" options shown on the event registration form. A bare reference
 * table like {@code service} — no publish window or soft delete, {@code is_active} is the only
 * visibility gate. event_registration.referral_group_id is ON DELETE SET NULL, so deleting a
 * row here blanks the field on any registration that referenced it — see
 * ReferralGroupService.delete for the same confirm-required guard Service uses.
 */
@Entity
@Table(name = "referral_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToOne(mappedBy = "referralGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ReferralGroupText text;
}
