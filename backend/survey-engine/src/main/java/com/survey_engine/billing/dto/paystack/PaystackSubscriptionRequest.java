package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for creating a subscription in Paystack.
 *
 * @param customer The customer code or email.
 * @param plan The plan code.
 * @param authorization The authorization code (optional, for existing authorizations).
 */
public record PaystackSubscriptionRequest(
        String customer,
        String plan,
        String authorization
) {}