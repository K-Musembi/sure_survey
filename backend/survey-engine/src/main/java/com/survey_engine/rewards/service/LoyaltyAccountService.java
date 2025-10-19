package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.LoyaltyAccountResponse;
import com.survey_engine.rewards.models.LoyaltyAccount;
import com.survey_engine.rewards.repository.LoyaltyAccountRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service class for managing loyalty accounts.
 * Handles creation, retrieval, and balance inquiries for user loyalty accounts.
 */
@Service
@RequiredArgsConstructor
public class LoyaltyAccountService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final UserApi userApi;

    /**
     * Finds an existing loyalty account for a user within the current tenant, or creates a new one if none exists.
     *
     * @param userId The ID of the user.
     * @return The found or newly created {@link LoyaltyAccount}.
     */
    @Transactional
    public LoyaltyAccount findOrCreateAccount(String userId) {
        Long tenantId = userApi.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> {
                    LoyaltyAccount newAccount = new LoyaltyAccount();
                    newAccount.setTenantId(tenantId);
                    newAccount.setUserId(userId);
                    newAccount.setBalance(BigDecimal.ZERO);
                    return loyaltyAccountRepository.save(newAccount);
                });
    }

    /**
     * Finds a loyalty account for a specific user within the current tenant.
     *
     * @param userId The ID of the user.
     * @return A {@link LoyaltyAccountResponse} DTO representing the found loyalty account.
     * @throws EntityNotFoundException if no loyalty account is found for the given user and tenant.
     */
    @Transactional(readOnly = true)
    public LoyaltyAccountResponse findAccountByUserId(String userId) {
        Long tenantId = userApi.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(this::mapToLoyaltyAccountResponse)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for user: " + userId));
    }

    /**
     * Retrieves the current loyalty point balance for a specific user within the current tenant.
     *
     * @param userId The ID of the user.
     * @return The current balance of the loyalty account, or {@link BigDecimal#ZERO} if no account is found.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        Long tenantId = userApi.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Maps a {@link LoyaltyAccount} entity to a {@link LoyaltyAccountResponse} DTO.
     *
     * @param account The {@link LoyaltyAccount} entity to map.
     * @return The corresponding {@link LoyaltyAccountResponse} DTO.
     */
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
