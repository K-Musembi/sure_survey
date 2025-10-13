package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.LoyaltyAccountResponse;
import com.survey_engine.rewards.models.LoyaltyAccount;
import com.survey_engine.rewards.repository.LoyaltyAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service class for managing user Loyalty Accounts.
 * This service provides methods to retrieve account details and balances.
 */
@Service
@RequiredArgsConstructor
public class LoyaltyAccountService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    /**
     * Finds a loyalty account for a user, creating one if it doesn't exist.
     * This is a key method for ensuring users automatically get an account when they earn points.
     *
     * @param userId The ID of the user.
     * @return The existing or newly created LoyaltyAccount entity.
     */
    @Transactional
    public LoyaltyAccount findOrCreateAccount(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    LoyaltyAccount newAccount = new LoyaltyAccount();
                    newAccount.setUserId(userId);
                    newAccount.setBalance(BigDecimal.ZERO);
                    return loyaltyAccountRepository.save(newAccount);
                });
    }

    /**
     * Finds a loyalty account by the user's ID.
     *
     * @param userId The ID of the user.
     * @return A DTO representing the loyalty account.
     * @throws EntityNotFoundException if no account is found for the user.
     */
    @Transactional(readOnly = true)
    public LoyaltyAccountResponse findAccountByUserId(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .map(this::mapToLoyaltyAccountResponse)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for user: " + userId));
    }

    /**
     * Retrieves the point balance for a specific user.
     * If the user does not have an account, it returns a balance of zero.
     *
     * @param userId The ID of the user.
     * @return The user's current loyalty point balance.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private LoyaltyAccountResponse mapToLoyaltyAccountResponse(LoyaltyAccount account) {
        return new LoyaltyAccountResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}