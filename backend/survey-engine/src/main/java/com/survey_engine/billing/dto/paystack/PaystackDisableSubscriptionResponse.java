package com.survey_engine.billing.dto.paystack;

/**
 * Response DTO for disabling (cancelling) a subscription in Paystack.
 *
 * @param status Indicates if the API call was successful.
 * @param message A descriptive message about the API call result.
 */
public record PaystackDisableSubscriptionResponse(
        boolean status,
        String message
) {}
