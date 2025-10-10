package com.survey_engine.payments.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request Data Transfer Object (DTO) for initiating a payment.
 * The userId will be extracted from the JWT token, not passed in the body.
 *
 * @param amount         The amount to be charged.
 * @param currency       The three-letter ISO currency code.
 * @param surveyId       The ID of the survey being paid for.
 * @param idempotencyKey A unique client-generated key to ensure idempotency.
 */
public record PaymentEventRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        @NotBlank(message = "Survey ID is required")
        String surveyId,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey
) {}
