package com.survey_engine.common.events;

import jakarta.validation.constraints.NotBlank;

/**
 * Event published when any module needs to send a WhatsApp notification.
 *
 * @param to      The recipient's phone number in international format.
 * @param message The content of the message.
 */
public record WhatsAppNotificationEvent(
        @NotBlank String to,
        @NotBlank String message
) {}
