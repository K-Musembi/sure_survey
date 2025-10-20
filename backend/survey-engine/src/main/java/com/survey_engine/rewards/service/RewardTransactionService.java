package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.RewardTransactionResponse;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import com.survey_engine.rewards.repository.RewardRepository;

/**
 * Service class for managing Reward Transaction details.
 * This service handles the business logic for creating, retrieving, and updating reward fulfillment transactions.
 */
@Service
@RequiredArgsConstructor
public class RewardTransactionService {

    private final RewardTransactionRepository rewardTransactionRepository;
    private final RewardRepository rewardRepository;
    private final UserApi userApi;

    /**
     * Creates a new, pending reward transaction.
     * This is the first step in the reward fulfillment process.
     *
     * @param rewardId The ID of the parent reward configuration.
     * @param participantId The ID of the participant receiving the reward.
     * @param recipientIdentifier The identifier for delivery (e.g., phone number).
     * @return The newly created RewardTransaction entity.
     */
    @Transactional
    public RewardTransaction createPendingTransaction(UUID rewardId, String participantId, String recipientIdentifier) {
        Long tenantId = userApi.getTenantId();
        Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

        RewardTransaction transaction = new RewardTransaction();
        transaction.setReward(reward);
        transaction.setParticipantId(participantId);
        transaction.setRecipientIdentifier(recipientIdentifier);
        transaction.setStatus(RewardTransactionStatus.PENDING);
        return rewardTransactionRepository.save(transaction);
    }

    /**
     * Updates the status of an existing reward transaction.
     *
     * @param transactionId The ID of the transaction to update.
     * @param status The new status (e.g., SUCCESS, FAILED).
     * @param providerTransactionId The ID from the external reward_provider, if successful.
     * @param failureReason The reason for failure, if applicable.
     */
    @Transactional
    public void updateTransactionStatus(UUID transactionId, RewardTransactionStatus status, String providerTransactionId, String failureReason) {
        RewardTransaction transaction = rewardTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("RewardTransaction not found with id: " + transactionId));

        transaction.setStatus(status);
        transaction.setProviderTransactionId(providerTransactionId);
        transaction.setFailureReason(failureReason);
        rewardTransactionRepository.save(transaction);
    }

    /**
     * Finds a single reward transaction by its unique ID.
     *
     * @param transactionId The UUID of the transaction.
     * @return A DTO representing the reward transaction.
     * @throws EntityNotFoundException if the transaction is not found.
     */
    @Transactional(readOnly = true)
    public RewardTransactionResponse findTransactionById(UUID transactionId) {
        return rewardTransactionRepository.findById(transactionId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Reward transaction not found with id: " + transactionId));
    }

    /**
     * Finds all transactions associated with a specific reward campaign.
     *
     * @param rewardId The UUID of the reward.
     * @return A list of DTOs representing the reward transactions.
     */
    @Transactional(readOnly = true)
    public List<RewardTransactionResponse> findTransactionsByRewardId(UUID rewardId) {
        return rewardTransactionRepository.findByRewardId(rewardId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a {@link RewardTransaction} entity to a {@link RewardTransactionResponse} DTO.
     *
     * @param transaction The {@link RewardTransaction} entity to map.
     * @return The corresponding {@link RewardTransactionResponse} DTO.
     */
    private RewardTransactionResponse mapToResponse(RewardTransaction transaction) {
        return new RewardTransactionResponse(
                transaction.getId(),
                transaction.getReward().getId(),
                transaction.getParticipantId(),
                transaction.getRecipientIdentifier(),
                transaction.getStatus(),
                transaction.getProviderTransactionId(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}