package com.survey_engine.survey.service.event_listener;

import com.survey_engine.common.events.SmsNotificationEvent;
import com.survey_engine.common.events.WhatsAppNotificationEvent;
import com.survey_engine.survey.service.sms.SmsSendingService;
import com.survey_engine.survey.service.whatsapp.WhatsAppSendingService;
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
    private final WhatsAppSendingService whatsAppSendingService;

    /**
     * Handles the request to send an SMS message.
     *
     * @param event The event containing the recipient and message details.
     */
    @EventListener
    public void handleSmsNotificationRequest(SmsNotificationEvent event) {
        log.info("Received SmsNotificationEvent for recipient: {}", event.to());
        try {
            smsSendingService.sendSms(event.to(), event.message());
        } catch (Exception e) {
            log.error("Failed to send SMS for notification request to {}: {}", event.to(), e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWhatsAppNotificationRequest(WhatsAppNotificationEvent event) {
        log.info("Received WhatsAppNotificationEvent for recipient: {}", event.to());
        try {
            whatsAppSendingService.sendTextMessage(event.to(), event.message());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", event.to(), e.getMessage(), e);
        }
    }
}
