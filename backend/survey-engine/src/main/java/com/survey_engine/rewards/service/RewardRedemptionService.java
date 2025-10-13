package com.survey_engine.rewards.service;

import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import com.survey_engine.survey.repository.ResponseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to handle the logic for a user claiming a reward after survey completion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardRedemptionService {

    private final RewardRepository rewardRepository;
    private final ResponseRepository responseRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final RewardFulfillmentService rewardFulfillmentService;

    /**
     * Allows an authenticated user to claim a reward for a survey they have completed.
     *
     * @param surveyId The ID of the survey.
     * @param phoneNumber The phone number to send the reward to.
     * @param userId The ID of the authenticated user making the claim.
     */
    @Transactional
    public void claimReward(Long surveyId, String phoneNumber, String userId) {
        log.info("Attempting to claim reward for surveyId: {} by userId: {}", surveyId, userId);

        // 1. Find the reward for the survey
        Reward reward = rewardRepository.findBySurveyId(String.valueOf(surveyId))
                .orElseThrow(() -> new EntityNotFoundException("No reward is configured for surveyId: " + surveyId));

        // 2. Check if reward is active
        if (reward.getStatus() != RewardStatus.ACTIVE || reward.getRemainingRewards() <= 0) {
            throw new IllegalStateException("This reward is no longer active or has been depleted.");
        }

        // 3. Verify the user actually completed this survey
        boolean userCompletedSurvey = responseRepository.findBySurveyId(surveyId).stream()
                .anyMatch(response -> userId.equals(response.getUserId()));

        if (!userCompletedSurvey) {
            throw new AccessDeniedException("You have not completed this survey and cannot claim a reward.");
        }

        // 4. Check if the user has already claimed this reward
        List<RewardTransaction> existingTransactions = rewardTransactionRepository.findByRewardId(reward.getId());
        boolean alreadyClaimed = existingTransactions.stream()
                .anyMatch(t -> userId.equals(t.getParticipantId()));

        if (alreadyClaimed) {
            throw new IllegalStateException("You have already claimed the reward for this survey.");
        }

        // 5. If all checks pass, delegate to the fulfillment service
        log.info("All checks passed. Delegating to RewardFulfillmentService for surveyId: {}, userId: {}, phoneNumber: {}",
                surveyId, userId, phoneNumber);
        
        // The fulfillment service needs the reward object and the recipient identifier (phone number)
        rewardFulfillmentService.disburse(reward, phoneNumber);
    }
}