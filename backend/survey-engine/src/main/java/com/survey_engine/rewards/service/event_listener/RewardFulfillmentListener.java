package com.survey_engine.rewards.service.event_listener;

import com.survey_engine.common.events.RewardDistributionEvent;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.reward_provider.RewardProvider;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    private final UserApi userApi;

    /**
     * Handles the {@link RewardDistributionEvent} event to initiate reward disbursement.
     *
     * @param event The event containing the reward and responder details.
     */
    @EventListener
    public void handleRewardDistributionRequest(RewardDistributionEvent event) {
        log.info("Received RewardDistributionEvent for rewardId: {} with responderId: {}", event.rewardId(), event.responderId());

        Reward reward = rewardRepository.findById(event.rewardId())
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + event.rewardId()));

        // For loyalty points, the responderId is the participantId, which is what the provider needs.
        if (reward.getRewardType() == RewardType.LOYALTY_POINTS) {
            disburseReward(reward, event.responderId());
            return;
        }

        // For other types like Airtime, we need to resolve the responderId to a phone number.
        resolvePhoneNumber(event.responderId()).ifPresentOrElse(
            phoneNumber -> disburseReward(reward, phoneNumber),
            () -> log.error("Could not resolve phone number for responderId: {}. Cannot disburse rewardId: {}", event.responderId(), reward.getId())
        );
    }

    private Optional<String> resolvePhoneNumber(String responderId) {
        // Simple check to see if it's a phone number (e.g., from an SMS session)
        if (responderId.matches("^\\+?[1-9]\\d{1,14}$")) { // Basic E.164 regex
            return Optional.of(responderId);
        }
        // Otherwise, assume it's a participantId and look it up.
        return userApi.findPhoneNumberByParticipantId(responderId);
    }

    private void disburseReward(Reward reward, String recipientIdentifier) {
        RewardProvider provider = rewardProviders.stream()
                .filter(p -> p.supports(reward.getRewardType()))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            log.error("No RewardProvider found for reward type: {}. Cannot disburse rewardId: {}",
                    reward.getRewardType(), reward.getId());
            return;
        }

        log.info("Found provider {} for reward type {}. Disbursing to {}",
                provider.getClass().getSimpleName(), reward.getRewardType(), recipientIdentifier);
        provider.disburse(reward, recipientIdentifier);
    }
}
