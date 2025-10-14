package com.survey_engine.rewards.dto;

import com.survey_engine.rewards.models.enums.RewardType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for creating a new Reward configuration for a survey.
 *
 * @param surveyId The ID of the survey to which this reward is linked.
 * @param rewardType The type of reward being offered (e.g., AIRTIME, LOYALTY_POINTS).
 * @param amountPerRecipient The value of the reward for each individual recipient.
 * @param currency The currency code for monetary rewards (e.g., KES, USD).
 * @param provider The external reward_provider for fulfilling the reward (e.g., Africa's Talking).
 * @param maxRecipients The total number of participants who can receive this reward.
 */
public record RewardRequest(
        @NotBlank(message = "Survey ID is required")
        String surveyId,

        @NotNull(message = "Reward type is required")
        RewardType rewardType,

        @NotNull(message = "Amount per recipient is required")
        @Min(value = 0, message = "Amount must be non-negative")
        BigDecimal amountPerRecipient,

        String currency,

        String provider,

        @NotNull(message = "Max recipients is required")
        @Min(value = 1, message = "Max recipients must be at least 1")
        Integer maxRecipients
) {
}