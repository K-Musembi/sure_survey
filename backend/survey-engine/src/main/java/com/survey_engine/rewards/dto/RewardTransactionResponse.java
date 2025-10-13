package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.RewardTransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning details of a reward fulfillment transaction.
 *
 * @param id The unique identifier of the transaction.
 * @param rewardId The ID of the parent Reward configuration.
 * @param participantId The ID of the participant who was meant to receive the reward.
 * @param recipientIdentifier The delivery identifier (e.g., phone number).
 * @param status The final status of the transaction (e.g., PENDING, SUCCESS, FAILED).
 * @param providerTransactionId The transaction ID from the external fulfillment provider.
 * @param failureReason A message detailing why a transaction failed, if applicable.
 * @param createdAt Timestamp of when the transaction was initiated.
 * @param updatedAt Timestamp of the last update.
 */
public record RewardTransactionResponse(
        UUID id,
        UUID rewardId,
        String participantId,
        String recipientIdentifier,
        RewardTransactionStatus status,
        String providerTransactionId,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}