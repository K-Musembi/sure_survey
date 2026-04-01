package com.survey_engine.survey.service;

import com.survey_engine.common.events.BusinessTransactionEvent;
import com.survey_engine.common.events.SmsNotificationEvent;
import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Listens for business transaction events (e.g. M-Pesa payments) and schedules
 * a delayed survey invitation with ODPC consent prompt.
 *
 * Flow: Payment received → 30 min delay → SMS consent prompt → respondent opts in → survey link sent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessTransactionSurveyListener {

    private final SurveyRepository surveyRepository;
    private final SurveyConsentService consentService;
    private final SystemSettingRepository settingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskScheduler taskScheduler;

    @Value("${survey.web.base-url}")
    private String webBaseUrl;

    private static final int DELAY_MINUTES = 30;

    @EventListener
    public void onBusinessTransaction(BusinessTransactionEvent event) {
        if (event.surveyId() == null || event.msisdn() == null) {
            log.debug("Ignoring business transaction without surveyId or msisdn: {}", event.transactionId());
            return;
        }

        log.info("Scheduling survey invitation for transaction {} survey {} phone {}",
                event.transactionId(), event.surveyId(), event.msisdn());

        // Schedule delayed survey invitation (30 minutes after transaction)
        taskScheduler.schedule(
                () -> sendSurveyInvitation(event),
                Instant.now().plusSeconds(DELAY_MINUTES * 60L)
        );
    }

    private void sendSurveyInvitation(BusinessTransactionEvent event) {
        try {
            Survey survey = surveyRepository.findById(event.surveyId()).orElse(null);
            if (survey == null) {
                log.warn("Survey {} not found for transaction invitation", event.surveyId());
                return;
            }

            if (!survey.isRequiresConsent()) {
                // No consent required — send survey link directly
                sendSurveyLink(survey, event);
                return;
            }

            // Build consent prompt with ODPC-compliant message
            String consentMessage = survey.getConsentMessage() != null
                    ? survey.getConsentMessage()
                    : consentService.getDefaultConsentMessage(survey.getName());

            String privacyUrl = settingRepository.findByKey(SettingKey.PRIVACY_NOTICE_URL)
                    .map(s -> s.getValue())
                    .orElse(null);

            StringBuilder sms = new StringBuilder();
            if (event.firstName() != null) {
                sms.append("Hi ").append(event.firstName()).append(", ");
            }
            sms.append(consentMessage);
            if (privacyUrl != null) {
                sms.append(" Privacy: ").append(privacyUrl);
            }

            eventPublisher.publishEvent(new SmsNotificationEvent(event.msisdn(), sms.toString()));
            log.info("Sent consent prompt for survey {} to {} (transaction {})",
                    event.surveyId(), event.msisdn(), event.transactionId());

        } catch (Exception e) {
            log.error("Failed to send survey invitation for transaction {}: {}",
                    event.transactionId(), e.getMessage(), e);
        }
    }

    private void sendSurveyLink(Survey survey, BusinessTransactionEvent event) {
        String surveyLink = webBaseUrl + survey.getUrlCode();
        StringBuilder sms = new StringBuilder();
        if (event.firstName() != null) {
            sms.append("Hi ").append(event.firstName()).append(", ");
        }
        sms.append("Please take a moment to share your feedback: ").append(surveyLink);

        eventPublisher.publishEvent(new SmsNotificationEvent(event.msisdn(), sms.toString()));
        log.info("Sent survey link for survey {} to {} (transaction {})",
                survey.getId(), event.msisdn(), event.transactionId());
    }
}
