package com.lucabridge.core.event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * PERSONAL DATA under the PDPO. ADMIN-only to read — see SecurityConfig's
 * /api/admin/registrations/** rule. eventId/referralGroupId are plain FK longs, not JPA
 * relations: nothing here routinely needs to navigate to the full Event or ReferralGroup, and
 * ReferralGroup doesn't have an entity yet (out of this task's scope) — an invalid id still
 * fails loudly via the DB's ON DELETE SET NULL foreign key, not silently.
 */
@Entity
@Table(name = "event_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** Shown to the registrant, used at check-in. Generated server-side — see ReferenceCodeGenerator. */
    @Column(name = "reference_code", nullable = false, unique = true, length = 20)
    private String referenceCode;

    @Column(name = "referral_group_id")
    private Long referralGroupId;

    @Column(name = "referral_group_other", length = 200)
    private String referralGroupOther;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /** @JdbcTypeCode forces VARCHAR: Hibernate's schema validator otherwise infers CHAR for a length-1 enum-as-string column, which doesn't match the actual varchar(1) column. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 1)
    private Gender gender;

    /** Year only, never a full DOB. */
    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(name = "postal_address", length = 500)
    private String postalAddress;

    @Column(name = "is_whatsapp_confirmed", nullable = false)
    @Builder.Default
    private boolean whatsappConfirmed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    @Column(length = 2)
    private String locale;

    /**
     * Consent TEXT lives in the frontend's i18n files, never here — only the version, so which
     * wording each person agreed to can still be proven after the text changes. Timestamps are
     * set server-side at submission time (Instant.now()), never trusted from the client.
     */
    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @Column(name = "privacy_consent_at", nullable = false)
    private Instant privacyConsentAt;

    @Column(name = "privacy_version", nullable = false, length = 20)
    private String privacyVersion;

    /** Separate, OPTIONAL opt-in. Never bundled into the mandatory consent above. */
    @Column(name = "is_friends_opt_in", nullable = false)
    @Builder.Default
    private boolean friendsOptIn = false;

    /** Escape hatch, no migration needed to add a one-off question. Not exposed on the public submission DTO yet. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_answers")
    private String extraAnswers;

    @Column(name = "admin_note", columnDefinition = "text")
    private String adminNote;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private Instant submittedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
