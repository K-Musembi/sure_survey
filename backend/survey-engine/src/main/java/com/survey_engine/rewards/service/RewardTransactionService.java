package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.RewardTransactionResponse;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for retrieving Reward Transaction details.
 */
@Service
@RequiredArgsConstructor
public class RewardTransactionService {

    private final RewardTransactionRepository rewardTransactionRepository;

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

    private RewardTransactionResponse mapToResponse(com.survey_engine.rewards.models.RewardTransaction transaction) {
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