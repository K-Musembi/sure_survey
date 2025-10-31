package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for creating a customer in Paystack.
 *
 * @param email The email address of the customer.
 * @param firstName The first name of the customer.
 * @param lastName The last name of the customer.
 * @param phone The phone number of the customer.
 */
public record PaystackCustomerRequest(
        String email,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        String phone
) {}