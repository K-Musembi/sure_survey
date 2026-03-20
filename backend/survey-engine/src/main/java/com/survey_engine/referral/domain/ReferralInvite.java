package com.survey_engine.referral.domain;

import com.survey_engine.referral.domain.enums.InviteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referral_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "referral_code_id", nullable = false)
    private UUID referralCodeId;

    @Column(name = "referred_phone", nullable = false)
    private String referredPhone;

    @Column(name = "channel", nullable = false)
    private String channel = "SMS";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InviteStatus status = InviteStatus.INVITE_SENT;

    @Column(name = "referrer_notified", nullable = false)
    private boolean referrerNotified = false;

    @Column(name = "invite_sent_at", updatable = false)
    private LocalDateTime inviteSentAt;

    @Column(name = "opted_in_at")
    private LocalDateTime optedInAt;

    @Column(name = "action_completed_at")
    private LocalDateTime actionCompletedAt;

    @Column(name = "rewarded_at")
    private LocalDateTime rewardedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() { inviteSentAt = LocalDateTime.now(); }

    public boolean isTerminal() {
        return status == InviteStatus.OPTED_OUT
                || status == InviteStatus.INVALID
                || status == InviteStatus.EXPIRED
                || status == InviteStatus.REWARDED;
    }
}
