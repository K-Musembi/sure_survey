package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.LoyaltyAccountResponse;
import com.survey_engine.rewards.service.LoyaltyAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller for retrieving information about Loyalty Accounts.
 */
@RestController
@RequestMapping("/api/v1/rewards/loyalty-accounts")
@RequiredArgsConstructor
public class LoyaltyAccountController {

    private final LoyaltyAccountService loyaltyAccountService;

    /**
     * Retrieves a loyalty account by its associated user ID.
     *
     * @param userId The ID of the user.
     * @return A ResponseEntity containing the loyalty account details.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LoyaltyAccountResponse> getAccountByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(loyaltyAccountService.findAccountByUserId(userId));
    }

    /**
     * Retrieves the loyalty point balance for a specific user.
     *
     * @param userId The ID of the user.
     * @return A ResponseEntity containing the user's balance.
     */
    @GetMapping("/user/{userId}/balance")

    public ResponseEntity<BigDecimal> getBalance(@PathVariable String userId) {
        return ResponseEntity.ok(loyaltyAccountService.getBalance(userId));
    }
}
