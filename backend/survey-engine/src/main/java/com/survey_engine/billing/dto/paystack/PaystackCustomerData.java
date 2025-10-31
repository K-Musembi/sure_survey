package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the nested 'data' object within a Paystack customer API response.
 *
 * @param customerCode The unique code for the customer (e.g., CUS_xxxxxx).
 */
public record PaystackCustomerData(
        @JsonProperty("customer_code")
        String customerCode
) {}
