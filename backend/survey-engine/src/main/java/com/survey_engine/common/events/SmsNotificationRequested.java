package com.survey_engine.common.events;

import jakarta.validation.constraints.NotBlank;

/**
 * A generic event published when any module needs to send an SMS notification.
 * The module responsible for SMS sending will listen for this event.
 *
 * @param to The recipient's phone number.
 * @param message The content of the SMS message.
 */
public record SmsNotificationRequested(
        @NotBlank String to,
        @NotBlank String message
) {
}
