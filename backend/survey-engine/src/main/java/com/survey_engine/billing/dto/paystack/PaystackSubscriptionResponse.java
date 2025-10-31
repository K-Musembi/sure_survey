package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for creating or fetching a subscription in Paystack.
 *
 * @param status Indicates if the API call was successful.
 * @param message A descriptive message about the API call result.
 * @param data The subscription data returned by Paystack.
 */
public record PaystackSubscriptionResponse(
        boolean status,
        String message,
        PaystackSubscriptionData data
) {}