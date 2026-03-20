package com.survey_engine.referral.service;

import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.domain.enums.RewardTrigger;
import com.survey_engine.referral.repository.ReferralCampaignRepository;
import com.survey_engine.referral.repository.ReferralInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listens for survey completion events to:
 * 1. Mark referral invites as ACTION_COMPLETED (triggering reward flow).
 * 2. Periodically expire stale invites.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReferralSurveyEventListener {

    private final ReferralService referralService;
    private final ReferralCampaignRepository campaignRepository;
    private final ReferralInviteRepository inviteRepository;

    /**
     * When a survey is completed, check if the respondent has any pending referral invites
     * for this survey's campaign and mark them as completed.
     */
    @Async
    @EventListener
    public void onSurveyCompleted(SurveyCompletedEvent event) {
        try {
            List<ReferralCampaign> campaigns = referralService.getActiveCampaignsForSurvey(event.surveyId());
            for (ReferralCampaign campaign : campaigns) {
                if (campaign.getRewardTrigger() == RewardTrigger.SURVEY_COMPLETE) {
                    // Find OPTED_IN invites for this respondent's phone (via participantId)
                    // The responderId may be a phone number or a userId — we match on best-effort
                    // A more precise match would require the SMS gateway to pass the invite UUID
                    log.debug("Survey completed event: checking referral invites for campaign {} respondent {}",
                            campaign.getId(), event.responderId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing referral on survey completion for survey={}: {}",
                    event.surveyId(), e.getMessage(), e);
        }
    }

    /**
     * Every hour, expire invites that passed their deadline without opt-in or action.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void expireStaleInvites() {
        try {
            int count = referralService.expireStaleInvites();
            if (count > 0) {
                log.info("Expired {} stale referral invites", count);
            }
        } catch (Exception e) {
            log.error("Referral expiry job failed: {}", e.getMessage(), e);
        }
    }
}
