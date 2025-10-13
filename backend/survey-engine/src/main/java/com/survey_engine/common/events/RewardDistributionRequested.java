package com.survey_engine.common.events;

import java.util.UUID;

/**
 * An event published when a survey completion has been processed and is eligible
 * for a reward distribution attempt. This event decouples the survey completion
 * from the actual reward fulfillment logic.
 *
 * @param rewardId The unique identifier of the {@link com.survey_engine.rewards.models.Reward} configuration.
 * @param responderId The unique identifier of the participant who completed the survey.
 *                    This could be a user UUID or a phone number for SMS participants.
 */
public record RewardDistributionRequested(
        UUID rewardId,
        String responderId
) {
}