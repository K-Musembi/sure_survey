package com.survey_engine.rewards.service;

import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyEventListener {

    private final RewardRepository rewardRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final LoyaltyService loyaltyService;

    @EventListener
    @Transactional
    public void handleSurveyCompletion(SurveyCompletedEvent event) {
        log.info("Received SurveyCompletedEvent for surveyId: {}", event.surveyId());

        Optional<Reward> rewardOpt = rewardRepository.findBySurveyId(String.valueOf(event.surveyId()));

        if (rewardOpt.isEmpty()) {
            log.info("No reward configured for surveyId: {}. Skipping.", event.surveyId());
            return;
        }

        Reward reward = rewardOpt.get();

        if (reward.getRewardType() != RewardType.LOYALTY_POINTS) {
            log.info("Reward for surveyId: {} is not for loyalty points. Skipping.", event.surveyId());
            return;
        }

        if (reward.getStatus() != RewardStatus.ACTIVE || reward.getRemainingRewards() <= 0) {
            log.warn("Reward for surveyId: {} is not active or has been depleted. Status: {}, Remaining: {}.",
                    event.surveyId(), reward.getStatus(), reward.getRemainingRewards());
            return;
        }

        // Create a pending transaction record first
        RewardTransaction transaction = new RewardTransaction();
        transaction.setReward(reward);
        transaction.setParticipantId(event.responderId());
        transaction.setRecipientIdentifier(event.responderId()); // For loyalty, the identifier is the user/participant ID
        transaction.setStatus(RewardTransactionStatus.PENDING);
        RewardTransaction savedTransaction = rewardTransactionRepository.save(transaction);

        try {
            String description = String.format("Loyalty points for completing survey %s", event.surveyId());
            loyaltyService.creditPoints(event.responderId(), reward.getAmount(), description, savedTransaction.getId());

            // Update transaction to success
            savedTransaction.setStatus(RewardTransactionStatus.SUCCESS);
            rewardTransactionRepository.save(savedTransaction);

            // Decrement remaining rewards
            reward.setRemainingRewards(reward.getRemainingRewards() - 1);
            if (reward.getRemainingRewards() == 0) {
                reward.setStatus(RewardStatus.DEPLETED);
            }
            rewardRepository.save(reward);

            log.info("Successfully processed loyalty points reward for surveyId: {} and responderId: {}",
                    event.surveyId(), event.responderId());

        } catch (Exception e) {
            log.error("Failed to process loyalty points reward for surveyId: {}. Error: {}", event.surveyId(), e.getMessage());
            // Mark the transaction as failed
            savedTransaction.setStatus(RewardTransactionStatus.FAILED);
            savedTransaction.setFailureReason(e.getMessage());
            rewardTransactionRepository.save(savedTransaction);
            // Note: We are not re-throwing the exception to prevent the event from being re-processed unnecessarily.
            // A separate mechanism could be implemented for retries if needed.
        }
    }
}