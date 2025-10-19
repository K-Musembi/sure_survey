package com.survey_engine.rewards.service.reward_provider;

import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.LoyaltyTransactionService;
import com.survey_engine.rewards.service.RewardTransactionService;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A {@link RewardProvider} implementation for disbursing LOYALTY_POINTS.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoyaltyPointsProvider implements RewardProvider {

    private final LoyaltyTransactionService loyaltyTransactionService;
    private final RewardTransactionService rewardTransactionService;
    private final RewardRepository rewardRepository;
    private final UserApi userApi;

    @Override
    public boolean supports(RewardType rewardType) {
        return rewardType == RewardType.LOYALTY_POINTS;
    }

    /**
     * Handles the disbursement of loyalty points for a completed survey.
     * This process is transactional and includes creating a pending transaction,
     * crediting the points, and updating the transaction and reward status upon completion.
     * It locks the reward record to prevent race conditions.
     *
     * @param rewardId The ID of the {@link Reward} configuration object.
     * @param responderId The identifier of the recipient (user ID).
     */
    @Override
    @Transactional
    public void disburse(UUID rewardId, String responderId) {
        log.info("Attempting to disburse LOYALTY_POINTS for rewardId: {} to responderId: {}", rewardId, responderId);

        Long tenantId = userApi.getTenantId();
        Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId + " for tenant: " + tenantId));

        if (reward.getRemainingRewards() <= 0) {
            log.warn("Reward campaign {} is already depleted. Cannot disburse loyalty points to responderId: {}", rewardId, responderId);
            // Optionally, create a FAILED transaction here to log the attempt.
            return;
        }

        // For loyalty, the recipient identifier is the user/participant ID itself.
        RewardTransaction transaction = rewardTransactionService.createPendingTransaction(
                reward.getId(),
                responderId,
                responderId
        );

        try {
            String description = String.format("Loyalty points for completing survey %s", reward.getSurveyId());
            loyaltyTransactionService.creditPoints(responderId, reward.getAmountPerRecipient(), description, transaction.getId());

            rewardTransactionService.updateTransactionStatus(transaction.getId(), RewardTransactionStatus.SUCCESS, null, null);

            // Decrement remaining rewards and update status if depleted
            reward.setRemainingRewards(reward.getRemainingRewards() - 1);
            if (reward.getRemainingRewards() <= 0) {
                reward.setStatus(RewardStatus.DEPLETED);
                log.info("Reward campaign {} has been depleted.", reward.getId());
            }
            rewardRepository.save(reward);

            log.info("Successfully processed loyalty points reward for rewardId: {} and responderId: {}",
                    reward.getId(), responderId);

        } catch (Exception e) {
            log.error("Failed to process loyalty points reward for rewardId: {}. Error: {}", reward.getId(), e.getMessage(), e);
            rewardTransactionService.updateTransactionStatus(transaction.getId(), RewardTransactionStatus.FAILED, null, e.getMessage());
            // We don't re-throw, as the transaction is marked as FAILED. Retries would need a separate mechanism.
        }
    }
}
