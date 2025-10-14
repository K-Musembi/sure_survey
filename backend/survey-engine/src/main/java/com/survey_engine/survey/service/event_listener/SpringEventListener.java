package com.survey_engine.survey.service.event_listener;

import com.survey_engine.common.events.PaymentSuccessEvent;
import com.survey_engine.survey.service.SurveyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SpringEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SpringEventListener.class);

    private final SurveyService surveyService;

    @Autowired
    public SpringEventListener(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * Handles the event published when a payment has succeeded.
     * This listener triggers the activation of the corresponding survey.
     *
     * @param event The event payload containing details of the successful payment.
     */
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        logger.info("Received PaymentSuccessEvent for surveyId: {}. Attempting to activate survey.", event.surveyId());
        try {
            // The surveyId from the payment event is a String, so it needs to be parsed.
            Long surveyId = Long.parseLong(event.surveyId());
            surveyService.activatePaidSurvey(surveyId);
        } catch (NumberFormatException e) {
            logger.error("Could not parse surveyId from PaymentSuccessEvent: {}", event.surveyId(), e);
        } catch (Exception e) {
            logger.error("Failed to activate survey {} after payment success event.", event.surveyId(), e);
            // In a real-world scenario, you might want to publish another event here
            // for monitoring or to trigger a compensating transaction.
        }
    }
}
