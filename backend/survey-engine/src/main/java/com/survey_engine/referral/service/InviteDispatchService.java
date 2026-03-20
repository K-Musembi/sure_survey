package com.survey_engine.referral.service;

import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.events.SmsNotificationEvent;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.domain.ReferralInvite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Dispatches referral invites via SMS (or WhatsApp in future).
 * Appends the platform privacy notice URL to every message as required by the
 * Kenya Data Protection Act 2019 (ODPC).
 * Async to avoid blocking the main transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InviteDispatchService {

    private final ApplicationEventPublisher eventPublisher;
    private final SystemSettingRepository settingRepository;

    @Value("${app.base-url:http://localhost:5173}")
    private String appBaseUrl;

    @Async
    public void dispatch(ReferralInvite invite, ReferralCampaign campaign) {
        try {
            String privacyUrl = settingRepository.findByKey(SettingKey.PRIVACY_NOTICE_URL)
                    .map(s -> s.getValue())
                    .orElse(null);
            String message = buildMessage(invite, campaign, privacyUrl);
            eventPublisher.publishEvent(new SmsNotificationEvent(invite.getReferredPhone(), message));
            log.info("Dispatched referral invite {} to {}", invite.getId(), invite.getReferredPhone());
        } catch (Exception e) {
            // Log but don't fail — invite record is already created
            log.error("Failed to dispatch referral invite {}: {}", invite.getId(), e.getMessage(), e);
        }
    }

    private String buildMessage(ReferralInvite invite, ReferralCampaign campaign, String privacyUrl) {
        String body = switch (campaign.getCampaignType()) {
            case SURVEY_CLOSED, SURVEY_OPEN ->
                "You have been invited to participate in a survey. " +
                "Reply YES to take part or STOP to decline. " +
                "Survey link: " + appBaseUrl + "/ref/" + findCodeFromInvite(invite);
            case SERVICE ->
                "You have been referred to try our service. " +
                "Reply YES to learn more or STOP to opt out. " +
                "Visit: " + appBaseUrl + "/ref/" + findCodeFromInvite(invite);
        };
        return privacyUrl != null ? body + " Privacy: " + privacyUrl : body;
    }

    private String findCodeFromInvite(ReferralInvite invite) {
        // The code is embedded via invite.referralCodeId — the controller injects it into the message
        // For SMS, we use the invite UUID as the reply token
        return invite.getId().toString().substring(0, 8).toUpperCase();
    }
}
