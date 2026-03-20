package com.survey_engine.referral.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referral_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "referrer_user_id")
    private Long referrerUserId;

    @Column(name = "referrer_participant_id")
    private String referrerParticipantId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "total_invites", nullable = false)
    private int totalInvites = 0;

    @Column(name = "successful_referrals", nullable = false)
    private int successfulReferrals = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
