package com.survey_engine.referral.domain;

import com.survey_engine.referral.domain.enums.CampaignStatus;
import com.survey_engine.referral.domain.enums.CampaignType;
import com.survey_engine.referral.domain.enums.RewardTrigger;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referral_campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false)
    private CampaignType campaignType;

    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "closed_group_id")
    private UUID closedGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_trigger", nullable = false)
    private RewardTrigger rewardTrigger;

    @Column(name = "referrer_reward_type")
    private String referrerRewardType;   // AIRTIME | DATA | POINTS

    @Column(name = "referrer_reward_value")
    private BigDecimal referrerRewardValue;

    @Column(name = "max_referrals_per_user", nullable = false)
    private int maxReferralsPerUser = 3;

    @Column(name = "daily_referral_limit", nullable = false)
    private int dailyReferralLimit = 5;

    @Column(name = "invite_expiry_hours", nullable = false)
    private int inviteExpiryHours = 72;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "business_integration_id")
    private UUID businessIntegrationId;

    @Column(name = "purpose_description", columnDefinition = "TEXT")
    private String purposeDescription;

    @Column(name = "consent_version", nullable = false)
    private int consentVersion = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
