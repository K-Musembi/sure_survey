package com.survey_engine.rewards.service.provider;

import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardType;

/**
 * Defines the contract for a reward fulfillment provider.
 * Each implementation is responsible for handling the disbursement of a specific type of reward.
 * This follows the Strategy design pattern.
 */
public interface RewardProvider {

    /**
     * Checks if this provider can handle the given reward type.
     *
     * @param rewardType The {@link RewardType} to check.
     * @return {@code true} if the provider supports the type, {@code false} otherwise.
     */
    boolean supports(RewardType rewardType);

    /**
     * Executes the disbursement of the reward to the specified responder.
     * Implementations should handle the entire fulfillment lifecycle, including
     * creating transaction records, calling external services if necessary,
     * and updating the state of the reward and transaction entities.
     *
     * @param reward The {@link Reward} configuration object.
     * @param responderId The identifier of the recipient (e.g., user ID, phone number).
     */
    void disburse(Reward reward, String responderId);
}