package com.survey_engine.rewards.service.notifications;

import com.survey_engine.common.events.SmsNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation of {@link NotificationService} for sending SMS notifications
 * by publishing {@link SmsNotificationEvent}s.
 */
@Service
@RequiredArgsConstructor
public class SmsNotificationService implements NotificationService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Sends an SMS message by publishing an {@link SmsNotificationEvent}.
     * The actual sending of the SMS is handled by an event listener.
     *
     * @param to The recipient's phone number.
     * @param message The content of the SMS message.
     */
    @Override
    public void sendSms(String to, String message) {
        eventPublisher.publishEvent(new SmsNotificationEvent(to, message));
    }
}