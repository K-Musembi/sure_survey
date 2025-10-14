package com.survey_engine.rewards.service.event_listener;

import com.survey_engine.common.events.RewardDistributionEvent;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.reward_provider.RewardProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for orchestrating the fulfillment of rewards.
 * It listens for {@link RewardDistributionEvent} events and delegates
 * the disbursement logic to the appropriate {@link RewardProvider}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardFulfillmentListener {

    private final List<RewardProvider> rewardProviders;
    private final RewardRepository rewardRepository;

    /**
     * Handles the {@link RewardDistributionEvent} event to initiate reward disbursement.
     *
     * @param event The event containing the reward and responder details.
     */
    @EventListener
    public void handleRewardDistributionRequest(RewardDistributionEvent event) {
        log.info("Received RewardDistributionEvent for rewardId: {}", event.rewardId());

        Reward reward = rewardRepository.findById(event.rewardId())
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + event.rewardId()));

        // Find a reward_provider that supports this reward type
        RewardProvider provider = rewardProviders.stream()
                .filter(p -> p.supports(reward.getRewardType()))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            log.error("No RewardProvider found for reward type: {}. Cannot disburse rewardId: {}",
                    reward.getRewardType(), reward.getId());
            // In a real system, you might want to mark the transaction as failed here.
            return;
        }

        log.info("Found reward_provider {} for reward type {}", provider.getClass().getSimpleName(), reward.getRewardType());
        provider.disburse(reward, event.responderId());
    }
}