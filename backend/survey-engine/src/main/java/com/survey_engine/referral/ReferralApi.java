package com.survey_engine.referral;

import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.dto.SendInviteRequest;
import com.survey_engine.referral.dto.SendInviteResult;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.UUID;

/**
 * Public API for the referral module.
 * Other modules use this interface — never reference internal classes directly.
 */
@NamedInterface("referral")
public interface ReferralApi {

    /**
     * Get all active campaigns configured for a survey.
     * Used by the survey module after a response is submitted.
     */
    List<ReferralCampaign> getActiveCampaignsForSurvey(Long surveyId);

    /**
     * Send a referral invite.
     */
    SendInviteResult sendInvite(SendInviteRequest request);

    /**
     * Mark the referred action as completed, triggering reward flow.
     */
    void onActionCompleted(UUID inviteId);
}
