package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning details of a Reward configuration.
 *
 * @param id The unique identifier of the reward.
 * @param surveyId The ID of the associated survey.
 * @param userId The ID of the user who configured the reward.
 * @param rewardType The type of reward.
 * @param amountPerRecipient The value of the reward for each recipient.
 * @param currency The currency of the reward.
 * @param provider The fulfillment provider.
 * @param maxRecipients The maximum number of recipients.
 * @param remainingRewards The number of rewards still available.
 * @param status The current status of the reward campaign (e.g., ACTIVE, DEPLETED).
 * @param createdAt Timestamp of when the reward was created.
 * @param updatedAt Timestamp of the last update.
 */
public record RewardResponse(
        UUID id,
        String surveyId,
        String userId,
        RewardType rewardType,
        BigDecimal amountPerRecipient,
        String currency,
        String provider,
        Integer maxRecipients,
        Integer remainingRewards,
        RewardStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}