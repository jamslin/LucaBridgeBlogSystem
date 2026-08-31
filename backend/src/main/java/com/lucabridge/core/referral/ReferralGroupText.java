package com.lucabridge.core.referral;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "referral_group_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralGroupText {

    @Id
    @Column(name = "referral_group_id")
    private Long referralGroupId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "referral_group_id")
    private ReferralGroup referralGroup;

    @Column(name = "tc_name", nullable = false, length = 200)
    private String tcName;

    @Column(name = "en_name", length = 200)
    private String enName;

    @Column(name = "sc_name", length = 200)
    private String scName;
}
