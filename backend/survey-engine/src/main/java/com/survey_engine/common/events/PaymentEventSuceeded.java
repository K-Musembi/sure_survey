package com.survey_engine.common.events;

import java.util.UUID;

/**
 * A Data Transfer Object (DTO) representing the payload for a successful payment event.
 * This record serves as the data contract for inter-module communication, published
 * via Spring Application Events when a payment is confirmed.
 * It decouples the consuming module (e.g., 'survey') from the internal domain models
 * of the 'payments' module.
 *
 * @param paymentId The unique ID of the internal payment record.
 * @param surveyId  The identifier for the survey that was paid for.
 * @param userId    The identifier for the user who made the payment.
 */
public record PaymentEventSuceeded(
        UUID paymentId,
        String surveyId,
        String userId
        //PaymentStatus status
) {}
