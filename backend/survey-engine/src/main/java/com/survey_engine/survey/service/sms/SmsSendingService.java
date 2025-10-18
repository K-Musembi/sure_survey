package com.survey_engine.survey.service.sms;

import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsSendingService {

    private final SmsService smsService;

    /**
     * Sends an SMS message to a single recipient using the configured Africa's Talking service.
     *
     * @param to      The recipient's phone number in international format (e.g., +254712345678).
     * @param message The content of the SMS message.
     */
    public void sendSms(String to, String message) {
        log.info("Attempting to send SMS to: {}", to);
        // The SDK requires an array of recipients
        final String[] recipients = new String[]{to};

        try {
            // The `true` parameter is for enqueueing the message
            List<Recipient> response = smsService.send(message, recipients, true);
            response.forEach(recipient -> {
                if ("Success".equals(recipient.status)) {
                    log.info("Successfully sent SMS to {}. MessageId: {}, Cost: {}", recipient.number, recipient.messageId, recipient.cost);
                } else {
                    log.error("Failed to send SMS to {}. Status: {}, Reason: {}", recipient.number, recipient.status, recipient.status);
                }
            });
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending SMS to {}: {}", to, e.getMessage());
        }
    }
}

