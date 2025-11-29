package com.survey_engine.rewards.service.event_listener;

import com.survey_engine.billing.BillingApi;
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
import java.util.UUID;

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
    private final BillingApi billingApi;

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
            disburseReward(reward, event.responderId(), reward.getRewardType());
            return;
        }

        // For other types like Airtime, we need to resolve the responderId to a phone number.
        resolvePhoneNumber(event.responderId()).ifPresentOrElse(
            phoneNumber -> disburseReward(reward, phoneNumber, reward.getRewardType()),
            () -> {
                log.error("Could not resolve phone number for responderId: {}. Cannot disburse rewardId: {}", event.responderId(), reward.getId());
                // TODO: Logic to rollback reservation would technically be needed here, but since we can't deliver, we might want to keep it pending or fail it.
                // For now, just logging.
            }
        );
    }

    /**
     * Resolves the responder ID to a phone number if it's not already one.
     * This is used for reward types like AIRTIME or DATA_BUNDLE.
     *
     * @param responderId The identifier of the responder (can be a phone number or participant ID).
     * @return An {@link Optional} containing the resolved phone number, or empty if it cannot be resolved.
     */
    private Optional<String> resolvePhoneNumber(String responderId) {
        // Simple check to see if it's a phone number (e.g., from an SMS session)
        if (responderId.matches("^\\+?[1-9]\\d{1,14}$")) { // Basic E.164 regex
            return Optional.of(responderId);
        }
        // Otherwise, assume it's a participantId and look it up.
        return userApi.findPhoneNumberByParticipantId(responderId);
    }

    /**
     * Delegates the actual reward disbursement to the appropriate {@link RewardProvider}.
     *
     * @param reward The reward configuration object.
     * @param recipientIdentifier The identifier of the recipient (e.g., phone number or user ID).
     * @param rewardType The type of reward to disburse.
     */
    private void disburseReward(Reward reward, String recipientIdentifier, RewardType rewardType) {
        RewardProvider provider = rewardProviders.stream()
                .filter(p -> p.supports(rewardType))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            log.error("No RewardProvider found for reward type: {}. Cannot disburse rewardId: {}",
                    rewardType, reward.getId());
            return;
        }

        log.info("Found provider {} for reward type {}. Disbursing to {}",
                provider.getClass().getSimpleName(), rewardType, recipientIdentifier);
                        try {
                            provider.disburse(reward.getId(), recipientIdentifier);

                            // If successful (sync providers), commit reservation
                            if (rewardType != RewardType.LOYALTY_POINTS) {
                                String walletType = rewardType == RewardType.AIRTIME ? "AIRTIME_STOCK" : "DATA_BUNDLE_STOCK";
                                billingApi.commitSystemReservation(walletType, reward.getAmountPerRecipient());
                            }
                        } catch (Exception e) {
                            log.error("Disbursement failed for rewardId {}. Rolling back reservation.", reward.getId(), e);
                            if (rewardType != RewardType.LOYALTY_POINTS) {
                                String walletType = rewardType == RewardType.AIRTIME ? "AIRTIME_STOCK" : "DATA_BUNDLE_STOCK";
                                billingApi.rollbackSystemReservation(walletType, reward.getAmountPerRecipient());
                            }
                        }
                    }
                }
        
                
        
        