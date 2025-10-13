package com.survey_engine.rewards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for a request to debit (spend) points from a loyalty account.
 *
 * @param amount The number of points to debit. Must be a positive value.
 * @param description A brief description of why the points are being debited (e.g., "Redeemed for $5 Voucher").
 */
public record LoyaltyDebitRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        String description
) {
}
