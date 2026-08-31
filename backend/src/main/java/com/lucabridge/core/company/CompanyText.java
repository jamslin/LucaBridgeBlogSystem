package com.lucabridge.core.company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyText {

    @Id
    @Column(name = "company_id")
    private Short companyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "tc_name", nullable = false, length = 200)
    private String tcName;

    @Column(name = "en_name", length = 200)
    private String enName;

    @Column(name = "sc_name", length = 200)
    private String scName;

    @Column(name = "tc_tagline", length = 300)
    private String tcTagline;

    @Column(name = "en_tagline", length = 300)
    private String enTagline;

    @Column(name = "sc_tagline", length = 300)
    private String scTagline;

    @Column(name = "tc_about", columnDefinition = "text")
    private String tcAbout;

    @Column(name = "en_about", columnDefinition = "text")
    private String enAbout;

    @Column(name = "sc_about", columnDefinition = "text")
    private String scAbout;

    @Column(name = "tc_address", length = 500)
    private String tcAddress;

    @Column(name = "en_address", length = 500)
    private String enAddress;

    @Column(name = "sc_address", length = 500)
    private String scAddress;

    @Column(name = "tc_office_hours", length = 300)
    private String tcOfficeHours;

    @Column(name = "en_office_hours", length = 300)
    private String enOfficeHours;

    @Column(name = "sc_office_hours", length = 300)
    private String scOfficeHours;
}
