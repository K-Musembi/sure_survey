package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the nested 'data' object within a Paystack subscription API response.
 *
 * @param subscriptionCode The unique code for the subscription (e.g., SUB_xxxxxx).
 * @param emailToken The email token for the subscription, used for confirmation of actions like cancellation.
 */
public record PaystackSubscriptionData(
        @JsonProperty("subscription_code")
        String subscriptionCode,
        @JsonProperty("email_token")
        String emailToken
) {}
