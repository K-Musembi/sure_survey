package com.payments.payments.dto.paystack;

import com.payments.payments.config.security.xss.Sanitize;

/**
 * Represents the top-level structure of a webhook event sent from PayStack.
 * It contains the event type and the associated data payload.
 *
 * @param <T> The type of the data payload, allowing for different event structures.
 */
public record PaystackWebhookEvent<T>(
    @Sanitize
    String event,
    T data
) {}