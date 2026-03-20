package com.survey_engine.referral;

import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.dto.SendInviteRequest;
import com.survey_engine.referral.dto.SendInviteResult;
import com.survey_engine.referral.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralApiImpl implements ReferralApi {

    private final ReferralService referralService;

    @Override
    public List<ReferralCampaign> getActiveCampaignsForSurvey(Long surveyId) {
        return referralService.getActiveCampaignsForSurvey(surveyId);
    }

    @Override
    public SendInviteResult sendInvite(SendInviteRequest request) {
        return referralService.sendInvite(request);
    }

    @Override
    public void onActionCompleted(UUID inviteId) {
        referralService.onActionCompleted(inviteId);
    }
}
