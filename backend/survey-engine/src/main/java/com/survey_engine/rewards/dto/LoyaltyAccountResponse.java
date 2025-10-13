package com.survey_engine.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning details of a user's loyalty account.
 *
 * @param id The unique identifier of the loyalty account.
 * @param userId The user associated with this account.
 * @param balance The current points balance.
 * @param createdAt Timestamp of when the account was created.
 * @param updatedAt Timestamp of the last update.
 */
public record LoyaltyAccountResponse(
        UUID id,
        String userId,
        BigDecimal balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}