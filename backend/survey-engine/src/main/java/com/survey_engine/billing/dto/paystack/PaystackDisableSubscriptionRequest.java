package com.survey_engine.billing.dto.paystack;

/**
 * Request DTO for disabling (cancelling) a subscription in Paystack.
 *
 * @param code The subscription code.
 * @param token The email token for confirmation.
 */
public record PaystackDisableSubscriptionRequest(
        String code,
        String token
) {}