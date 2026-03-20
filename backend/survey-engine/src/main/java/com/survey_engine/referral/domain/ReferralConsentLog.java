package com.survey_engine.referral.domain;

import com.survey_engine.referral.domain.enums.ConsentEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable ODPC-compliant consent audit record.
 * Never update rows in this table — only insert.
 */
@Entity
@Table(name = "referral_consent_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralConsentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "referral_invite_id", nullable = false, updatable = false)
    private UUID referralInviteId;

    @Column(name = "phone", nullable = false, updatable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private ConsentEventType eventType;

    @Column(name = "channel", updatable = false)
    private String channel;

    @Column(name = "referrer_confirmed_consent", updatable = false)
    private boolean referrerConfirmedConsent;

    /** Immutable snapshot of the campaign's purpose_description at consent time. */
    @Column(name = "purpose_snapshot", updatable = false, columnDefinition = "TEXT")
    private String purposeSnapshot;

    /** Campaign consent_version at the time this consent was recorded. */
    @Column(name = "consent_version", nullable = false, updatable = false)
    private int consentVersion = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
