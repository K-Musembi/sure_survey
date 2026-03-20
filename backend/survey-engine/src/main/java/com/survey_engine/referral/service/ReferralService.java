package com.survey_engine.referral.service;

import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.referral.domain.*;
import com.survey_engine.referral.domain.enums.CampaignStatus;
import com.survey_engine.referral.domain.enums.CampaignType;
import com.survey_engine.referral.domain.enums.ConsentEventType;
import com.survey_engine.referral.domain.enums.InviteStatus;
import com.survey_engine.referral.dto.*;
import com.survey_engine.referral.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralCampaignRepository campaignRepository;
    private final ReferralCodeRepository codeRepository;
    private final ReferralInviteRepository inviteRepository;
    private final ReferralConsentLogRepository consentLogRepository;
    private final ReferralClosedGroupRepository closedGroupRepository;
    private final InviteDispatchService dispatchService;

    @Transactional
    public ReferralCampaign createCampaign(Long tenantId, CampaignRequest request) {
        ReferralCampaign campaign = new ReferralCampaign();
        campaign.setTenantId(tenantId);
        campaign.setName(request.name());
        campaign.setCampaignType(request.campaignType());
        campaign.setSurveyId(request.surveyId());
        campaign.setClosedGroupId(request.closedGroupId());
        campaign.setRewardTrigger(request.rewardTrigger());
        campaign.setReferrerRewardType(request.referrerRewardType());
        campaign.setReferrerRewardValue(request.referrerRewardValue());
        campaign.setMaxReferralsPerUser(request.maxReferralsPerUser() > 0 ? request.maxReferralsPerUser() : 3);
        campaign.setDailyReferralLimit(request.dailyReferralLimit() > 0 ? request.dailyReferralLimit() : 5);
        campaign.setInviteExpiryHours(request.inviteExpiryHours() > 0 ? request.inviteExpiryHours() : 72);
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        campaign.setPurposeDescription(request.purposeDescription());
        campaign.setConsentVersion(1);
        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaignRepository.save(campaign);
    }

    public List<ReferralCampaign> getCampaignsForTenant(Long tenantId) {
        return campaignRepository.findByTenantId(tenantId);
    }

    @Transactional
    public void updateCampaignStatus(UUID campaignId, CampaignStatus newStatus) {
        ReferralCampaign campaign = getCampaignOrThrow(campaignId);
        campaign.setStatus(newStatus);
        campaignRepository.save(campaign);
    }

    /**
     * Updates the campaign's stated purpose and increments consent_version.
     * Any future opt-ins will snapshot the new purpose. Existing opted-in invites
     * whose consent version is now stale will be re-prompted on next action completion.
     */
    @Transactional
    public ReferralCampaign updateCampaignPurpose(UUID campaignId, String newPurpose) {
        ReferralCampaign campaign = getCampaignOrThrow(campaignId);
        campaign.setPurposeDescription(newPurpose);
        campaign.setConsentVersion(campaign.getConsentVersion() + 1);
        log.info("Campaign {} purpose updated to version {}", campaignId, campaign.getConsentVersion());
        return campaignRepository.save(campaign);
    }

    public ReferralCode getOrCreateCode(UUID campaignId, Long referrerUserId) {
        return codeRepository.findByCampaignIdAndReferrerUserId(campaignId, referrerUserId)
                .orElseGet(() -> {
                    ReferralCode code = new ReferralCode();
                    code.setCampaignId(campaignId);
                    code.setReferrerUserId(referrerUserId);
                    code.setCode(generateUniqueCode());
                    return codeRepository.save(code);
                });
    }

    @Transactional
    public SendInviteResult sendInvite(SendInviteRequest request) {
        ReferralCampaign campaign = getCampaignOrThrow(request.campaignId());

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new BusinessRuleException("REFERRAL_CAMPAIGN_INACTIVE", "This referral campaign is not active.");
        }

        // Rate limit: daily cap
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCount = inviteRepository.countTodayInvites(
                request.referrerUserId(), request.campaignId(), startOfDay);
        if (todayCount >= campaign.getDailyReferralLimit()) {
            throw new BusinessRuleException("REFERRAL_DAILY_LIMIT_EXCEEDED",
                    "Daily referral limit of " + campaign.getDailyReferralLimit() + " reached.");
        }

        // Duplicate check
        if (inviteRepository.existsActiveInvite(request.referredPhone(), request.campaignId())) {
            throw new BusinessRuleException("REFERRAL_DUPLICATE_INVITE",
                    "An active invite has already been sent to this contact.");
        }

        // Closed group validation
        if (campaign.getCampaignType() == CampaignType.SURVEY_CLOSED) {
            if (campaign.getClosedGroupId() == null ||
                    !closedGroupRepository.isMember(campaign.getClosedGroupId(), request.referredPhone())) {
                log.info("Referral rejected: {} not in closed group for campaign {}",
                        request.referredPhone(), request.campaignId());
                return SendInviteResult.invalid("The referred contact is not eligible for this survey.");
            }
        }

        ReferralCode code = getOrCreateCode(request.campaignId(), request.referrerUserId());

        // Create invite record
        ReferralInvite invite = new ReferralInvite();
        invite.setReferralCodeId(code.getId());
        invite.setReferredPhone(request.referredPhone());
        invite.setChannel(request.channel() != null ? request.channel() : "SMS");
        invite.setExpiresAt(LocalDateTime.now().plusHours(campaign.getInviteExpiryHours()));

        // For SERVICE and SURVEY_OPEN campaigns: request double opt-in
        if (campaign.getCampaignType() == CampaignType.SERVICE ||
                campaign.getCampaignType() == CampaignType.SURVEY_OPEN) {
            invite.setStatus(InviteStatus.OPT_IN_REQUESTED);
        } else {
            // SURVEY_CLOSED: phone already validated as member — can proceed directly
            invite.setStatus(InviteStatus.INVITE_SENT);
        }

        ReferralInvite saved = inviteRepository.save(invite);

        // Update code stats
        code.setTotalInvites(code.getTotalInvites() + 1);
        codeRepository.save(code);

        // Log referrer consent confirmation (ODPC) — snapshot campaign purpose at consent time
        if (request.referrerConfirmedConsent()) {
            logConsent(saved.getId(), request.referredPhone(), ConsentEventType.OPT_IN,
                    invite.getChannel(), true, campaign);
        }

        // Dispatch message asynchronously
        dispatchService.dispatch(saved, campaign);

        log.info("Referral invite sent: invite={} campaign={} phone={}",
                saved.getId(), campaign.getId(), request.referredPhone());

        return SendInviteResult.success(saved.getId(), code.getCode());
    }

    @Transactional
    public void processOptIn(UUID inviteId) {
        ReferralInvite invite = getInviteOrThrow(inviteId);
        assertTransitionAllowed(invite, InviteStatus.OPTED_IN);

        invite.setStatus(InviteStatus.OPTED_IN);
        invite.setOptedInAt(LocalDateTime.now());
        inviteRepository.save(invite);

        // Snapshot campaign purpose at opt-in time
        ReferralCampaign campaign = codeRepository.findById(invite.getReferralCodeId())
                .flatMap(c -> campaignRepository.findById(c.getCampaignId()))
                .orElse(null);
        logConsent(inviteId, invite.getReferredPhone(), ConsentEventType.OPT_IN, invite.getChannel(), false, campaign);
        log.info("Opt-in recorded for invite {}", inviteId);
    }

    @Transactional
    public void processOptOut(UUID inviteId) {
        ReferralInvite invite = getInviteOrThrow(inviteId);

        if (invite.isTerminal()) {
            log.warn("Opt-out on terminal invite {} — ignoring", inviteId);
            return;
        }

        invite.setStatus(InviteStatus.OPTED_OUT);
        inviteRepository.save(invite);

        logConsent(inviteId, invite.getReferredPhone(), ConsentEventType.OPT_OUT, invite.getChannel(), false, null);
        log.info("Opt-out recorded for invite {}", inviteId);
    }

    @Transactional
    public void onActionCompleted(UUID inviteId) {
        ReferralInvite invite = getInviteOrThrow(inviteId);
        assertTransitionAllowed(invite, InviteStatus.ACTION_COMPLETED);

        // Purpose limitation guard: ensure the consent version still matches the campaign.
        codeRepository.findById(invite.getReferralCodeId()).ifPresent(code -> {
            campaignRepository.findById(code.getCampaignId()).ifPresent(campaign -> {
                int latestConsentVersion = consentLogRepository
                        .findByReferralInviteId(inviteId)
                        .stream()
                        .mapToInt(ReferralConsentLog::getConsentVersion)
                        .max()
                        .orElse(1);
                if (latestConsentVersion < campaign.getConsentVersion()) {
                    throw new BusinessRuleException("CONSENT_VERSION_STALE",
                            "Consent for invite " + inviteId + " was granted under an older purpose. " +
                            "Re-consent is required before this action can be recorded.");
                }
            });
        });

        invite.setStatus(InviteStatus.ACTION_COMPLETED);
        invite.setActionCompletedAt(LocalDateTime.now());
        inviteRepository.save(invite);

        log.info("Referral action completed for invite {}", inviteId);
        // Reward triggering is handled by ReferralRewardService listening to this state change
    }

    @Transactional
    public void onRewarded(UUID inviteId) {
        ReferralInvite invite = getInviteOrThrow(inviteId);
        invite.setStatus(InviteStatus.REWARDED);
        invite.setRewardedAt(LocalDateTime.now());
        inviteRepository.save(invite);

        // Update successful referral counter on code
        codeRepository.findById(invite.getReferralCodeId()).ifPresent(code -> {
            code.setSuccessfulReferrals(code.getSuccessfulReferrals() + 1);
            codeRepository.save(code);
        });

        log.info("Referral reward issued for invite {}", inviteId);
    }

    @Transactional
    public int expireStaleInvites() {
        List<ReferralInvite> stale = inviteRepository
                .findByStatusAndExpiresAtBefore(InviteStatus.OPT_IN_REQUESTED, LocalDateTime.now());
        stale.addAll(inviteRepository
                .findByStatusAndExpiresAtBefore(InviteStatus.INVITE_SENT, LocalDateTime.now()));

        stale.forEach(invite -> invite.setStatus(InviteStatus.EXPIRED));
        inviteRepository.saveAll(stale);

        if (!stale.isEmpty()) {
            log.info("Expired {} stale referral invites", stale.size());
        }
        return stale.size();
    }

    public List<ReferralCampaign> getActiveCampaignsForSurvey(Long surveyId) {
        return campaignRepository.findBySurveyIdAndStatus(surveyId, CampaignStatus.ACTIVE);
    }

    private ReferralCampaign getCampaignOrThrow(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("REFERRAL_CAMPAIGN_NOT_FOUND",
                        "Referral campaign not found: " + id));
    }

    private ReferralInvite getInviteOrThrow(UUID id) {
        return inviteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("REFERRAL_INVITE_NOT_FOUND",
                        "Referral invite not found: " + id));
    }

    private void assertTransitionAllowed(ReferralInvite invite, InviteStatus targetStatus) {
        if (invite.isTerminal()) {
            throw new BusinessRuleException("REFERRAL_INVALID_STATE_TRANSITION",
                    "Cannot transition invite " + invite.getId() +
                            " from " + invite.getStatus() + " to " + targetStatus);
        }
    }

    private void logConsent(UUID inviteId, String phone, ConsentEventType type,
                            String channel, boolean referrerConfirmed, ReferralCampaign campaign) {
        ReferralConsentLog entry = new ReferralConsentLog();
        entry.setReferralInviteId(inviteId);
        entry.setPhone(phone);
        entry.setEventType(type);
        entry.setChannel(channel);
        entry.setReferrerConfirmedConsent(referrerConfirmed);
        if (campaign != null) {
            entry.setPurposeSnapshot(campaign.getPurposeDescription());
            entry.setConsentVersion(campaign.getConsentVersion());
        }
        consentLogRepository.save(entry);
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        String candidate = sb.toString();
        // Retry on collision (extremely rare)
        return codeRepository.findByCode(candidate).isPresent() ? generateUniqueCode() : candidate;
    }
}
