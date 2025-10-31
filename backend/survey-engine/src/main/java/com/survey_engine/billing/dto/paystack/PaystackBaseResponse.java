package com.survey_engine.billing.dto.paystack;

/**
 * Base record for Paystack API responses.
 *
 * @param status Indicates if the API call was successful.
 * @param message A descriptive message about the API call result.
 */
public record PaystackBaseResponse(
        boolean status,
        String message
) {}