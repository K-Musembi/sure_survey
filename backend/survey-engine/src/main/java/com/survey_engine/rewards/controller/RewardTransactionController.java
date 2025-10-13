package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.RewardTransactionResponse;
import com.survey_engine.rewards.service.RewardTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for retrieving Reward Transaction details.
 */
@RestController
@RequestMapping("/api/v1/rewards/reward-transactions")
@RequiredArgsConstructor
public class RewardTransactionController {

    private final RewardTransactionService rewardTransactionService;

    /**
     * Retrieves a single reward transaction by its unique ID.
     *
     * @param transactionId The UUID of the transaction.
     * @return A ResponseEntity containing the transaction details.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<RewardTransactionResponse> getTransactionById(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(rewardTransactionService.findTransactionById(transactionId));
    }

    /**
     * Retrieves all transactions for a specific reward campaign.
     *
     * @param rewardId The UUID of the reward.
     * @return A ResponseEntity containing a list of reward transactions.
     */
    @GetMapping("/reward/{rewardId}")
    public ResponseEntity<List<RewardTransactionResponse>> getTransactionsByRewardId(@PathVariable UUID rewardId) {
        return ResponseEntity.ok(rewardTransactionService.findTransactionsByRewardId(rewardId));
    }
}


