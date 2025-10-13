package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating a new reward transaction record.
 *
 * @param rewardId The ID of the parent Reward configuration.
 * @param participantId The ID of the participant receiving the reward.
 * @param recipientIdentifier The actual identifier for delivery (e.g., phone number, email).
 * @param status The initial status of the transaction.
 */
public record RewardTransactionRequest(
        @NotNull(message = "Reward ID is required")
        UUID rewardId,

        @NotBlank(message = "Participant ID is required")
        String participantId,

        @NotBlank(message = "Recipient identifier is required")
        String recipientIdentifier,

        @NotNull(message = "Status is required")
        RewardTransactionStatus status
) {
}