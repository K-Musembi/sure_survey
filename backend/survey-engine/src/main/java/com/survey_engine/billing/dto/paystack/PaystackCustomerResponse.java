package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for creating or fetching a customer in Paystack.
 *
 * @param status Indicates if the API call was successful.
 * @param message A descriptive message about the API call result.
 * @param data The customer data returned by Paystack.
 */
public record PaystackCustomerResponse(
        boolean status,
        String message,
        PaystackCustomerData data
) {}