package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.LoyaltyTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning details of a loyalty transaction.
 *
 * @param id The unique identifier of the transaction.
 * @param loyaltyAccountId The ID of the associated loyalty account.
 * @param rewardTransactionId The ID of the survey reward transaction that triggered this, if applicable.
 * @param type The type of transaction (CREDIT or DEBIT).
 * @param amount The number of points transacted.
 * @param description A description of the transaction.
 * @param createdAt Timestamp of when the transaction occurred.
 */
public record LoyaltyTransactionResponse(
        UUID id,
        UUID loyaltyAccountId,
        UUID rewardTransactionId,
        LoyaltyTransactionType type,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
}