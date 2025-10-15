package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.RewardRequest;
import com.survey_engine.rewards.dto.RewardResponse;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.repository.RewardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing Reward configurations.
 * This service handles the business logic for creating, retrieving, and cancelling reward campaigns.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    /**
     * Creates a new reward configuration for a survey.
     * Ensures that a survey does not have more than one reward configuration.
     *
     * @param rewardRequest The DTO containing the details for the new reward.
     * @param userId The ID of the user creating the reward, for ownership and authorization.
     * @return A DTO representing the newly created reward configuration.
     * @throws DataIntegrityViolationException if a reward for the given survey already exists.
     */
    @Transactional
    public RewardResponse createReward(RewardRequest rewardRequest, String userId) {
        rewardRepository.findBySurveyId(rewardRequest.surveyId()).ifPresent(r -> {
            throw new DataIntegrityViolationException("A reward for survey " + rewardRequest.surveyId() + " already exists.");
        });

        Reward reward = new Reward();
        reward.setSurveyId(rewardRequest.surveyId());
        reward.setUserId(userId);
        reward.setRewardType(rewardRequest.rewardType());
        reward.setAmountPerRecipient(rewardRequest.amountPerRecipient());
        reward.setCurrency(rewardRequest.currency());
        reward.setProvider(rewardRequest.provider());
        reward.setMaxRecipients(rewardRequest.maxRecipients());
        reward.setRemainingRewards(rewardRequest.maxRecipients());
        reward.setStatus(RewardStatus.ACTIVE);

        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

    /**
     * Finds the reward configuration for a specific survey.
     *
     * @param surveyId The ID of the survey.
     * @return A DTO representing the reward configuration.
     * @throws EntityNotFoundException if no reward is found for the survey.
     */
    @Transactional(readOnly = true)
    public RewardResponse findRewardBySurveyId(String surveyId) {
        return rewardRepository.findBySurveyId(surveyId)
                .map(this::mapToRewardResponse)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found for surveyId: " + surveyId));
    }

    /**
     * Finds all reward configurations created by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of DTOs representing the user's reward configurations.
     */
    @Transactional(readOnly = true)
    public List<RewardResponse> findRewardsByUserId(String userId) {
        return rewardRepository.findByUserId(userId).stream()
                .map(this::mapToRewardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancels a reward campaign.
     * This is an idempotent action that sets the reward status as CANCELLED.
     *
     * @param rewardId The ID of the reward to cancel.
     * @param userId The ID of the user attempting the action, for authorization.
     * @return A DTO representing the updated, cancelled reward.
     * @throws EntityNotFoundException if the reward is not found.
     * @throws AccessDeniedException if the user is not the owner of the reward.
     * @throws IllegalStateException if the reward has already been depleted.
     */
    @Transactional
    public RewardResponse cancelReward(UUID rewardId, String userId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

        if (!reward.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to cancel this reward.");
        }

        if (reward.getStatus() == RewardStatus.DEPLETED) {
            throw new IllegalStateException("Cannot cancel a depleted reward.");
        }

        reward.setStatus(RewardStatus.CANCELLED);
        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

    private RewardResponse mapToRewardResponse(Reward reward) {
        return new RewardResponse(
                reward.getId(),
                reward.getSurveyId(),
                reward.getUserId(),
                reward.getRewardType(),
                reward.getAmountPerRecipient(),
                reward.getCurrency(),
                reward.getProvider(),
                reward.getMaxRecipients(),
                reward.getRemainingRewards(),
                reward.getStatus(),
                reward.getCreatedAt(),
                reward.getUpdatedAt()
        );
    }
}