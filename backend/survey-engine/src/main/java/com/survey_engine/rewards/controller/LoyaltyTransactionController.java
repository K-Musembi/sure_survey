package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.LoyaltyTransactionResponse;
import com.survey_engine.rewards.service.LoyaltyTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**

 * Controller for retrieving Loyalty Transaction history.

 */

@RestController
@RequestMapping("/api/v1/rewards/loyalty-transactions")
@RequiredArgsConstructor
public class LoyaltyTransactionController {

    private final LoyaltyTransactionService loyaltyTransactionService;

    /**
     * Retrieves all transactions for a specific loyalty account.
     *
     * @param accountId The UUID of the loyalty account.
     * @return A ResponseEntity containing a list of loyalty transactions.
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getTransactionsForAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(loyaltyTransactionService.findTransactionsByAccountId(accountId));
    }
}
