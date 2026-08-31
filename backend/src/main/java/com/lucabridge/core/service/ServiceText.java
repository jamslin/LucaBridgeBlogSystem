package com.lucabridge.core.service;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceText {

    @Id
    @Column(name = "service_id")
    private Long serviceId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "tc_name", nullable = false, length = 120)
    private String tcName;

    @Column(name = "en_name", length = 120)
    private String enName;

    @Column(name = "sc_name", length = 120)
    private String scName;

    @Column(name = "tc_description", length = 500)
    private String tcDescription;

    @Column(name = "en_description", length = 500)
    private String enDescription;

    @Column(name = "sc_description", length = 500)
    private String scDescription;
}
