package com.survey_engine.survey.service;

import com.survey_engine.common.events.SmsNotificationRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Listens for generic notification events and uses module-specific services to handle them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final SmsSendingService smsSendingService;

    /**
     * Handles the request to send an SMS message.
     *
     * @param event The event containing the recipient and message details.
     */
    @EventListener
    public void handleSmsNotificationRequest(SmsNotificationRequested event) {
        log.info("Received SmsNotificationRequested for recipient: {}", event.to());
        try {
            smsSendingService.sendSms(event.to(), event.message());
        } catch (Exception e) {
            log.error("Failed to send SMS for notification request to {}: {}", event.to(), e.getMessage(), e);
            // Depending on requirements, a failure event could be published here.
        }
    }
}
