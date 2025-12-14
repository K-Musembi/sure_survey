package com.survey_engine.survey.service.event_listener;

import com.survey_engine.common.events.BusinessTransactionEvent;
import com.survey_engine.survey.service.DistributionListService;
import com.survey_engine.survey.service.sms.SmsResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessTransactionEventListener {

    private final DistributionListService distributionListService;
    private final SmsResponseService smsResponseService;

    @EventListener
    public void handleBusinessTransaction(BusinessTransactionEvent event) {
        log.info("Received business transaction event for survey {}. Customer: {}", event.surveyId(), event.msisdn());

        try {
            // 1. Add to distribution list (ensures we have a record)
            distributionListService.addContactToSurveyList(
                    event.surveyId(), event.msisdn(), event.firstName(),  event.lastName());

            // 2. Trigger SMS Survey
            smsResponseService.initiateSurvey(event.msisdn(), event.surveyId());
            
        } catch (Exception e) {
            log.error("Failed to process business transaction event for survey {}", event.surveyId(), e);
        }
    }
}
