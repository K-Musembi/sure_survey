package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.LoyaltyTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for creating a new loyalty transaction.
 *
 * @param accountId The ID of the loyalty account being affected.
 * @param type The type of transaction (CREDIT or DEBIT).
 * @param amount The number of points being transacted.
 * @param description A brief description of the transaction.
 */
public record LoyaltyTransactionRequest(
        @NotNull(message = "Account ID is required")
        String accountId,

        @NotNull(message = "Transaction type is required")
        LoyaltyTransactionType type,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        String description
) {
}