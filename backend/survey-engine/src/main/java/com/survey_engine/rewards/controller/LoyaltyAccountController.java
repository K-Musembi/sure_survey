package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.LoyaltyAccountResponse;
import com.survey_engine.rewards.dto.LoyaltyDebitRequest;
import com.survey_engine.rewards.service.LoyaltyAccountService;
import com.survey_engine.rewards.service.LoyaltyTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for retrieving information about Loyalty Accounts and performing actions.
 */
@RestController
@RequestMapping("/api/v1/rewards/loyalty-accounts")
@RequiredArgsConstructor
public class LoyaltyAccountController {

    private final LoyaltyAccountService loyaltyAccountService;
    private final LoyaltyTransactionService loyaltyTransactionService;

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

    /**
     * Debits a specified amount of points from the authenticated user's loyalty account.
     *
     * @param request The request body containing the amount and description for the debit.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the updated loyalty account details.
     */
    @PostMapping("/me/debit")
    public ResponseEntity<LoyaltyAccountResponse> debitPoints(
            @Valid @RequestBody LoyaltyDebitRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        loyaltyTransactionService.debitPoints(userId, request.amount(), request.description());
        // Return the updated account status
        return ResponseEntity.ok(loyaltyAccountService.findAccountByUserId(userId));
    }
}
