package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.LoyaltyTransactionResponse;
import com.survey_engine.rewards.models.LoyaltyAccount;
import com.survey_engine.rewards.models.LoyaltyTransaction;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.LoyaltyTransactionType;
import com.survey_engine.rewards.repository.LoyaltyAccountRepository;
import com.survey_engine.rewards.repository.LoyaltyTransactionRepository;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing loyalty transactions, including crediting and debiting points.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyTransactionService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final LoyaltyAccountService loyaltyAccountService; // Injected service
    private final UserApi userApi;

    /**
     * Credits a specified amount of points to a user's loyalty account.
     *
     * @param userId The ID of the user to credit.
     * @param amount The amount of points to credit. Must be positive.
     * @param description A description of the credit transaction.
     * @param rewardTransactionId The ID of the reward transaction that triggered this credit.
     */
    @Transactional
    public void creditPoints(String userId, BigDecimal amount, String description, UUID rewardTransactionId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to credit non-positive amount {} for user {}. Skipping.", amount, userId);
            return;
        }

        LoyaltyAccount account = loyaltyAccountService.findOrCreateAccount(userId);
        account.setBalance(account.getBalance().add(amount));
        loyaltyAccountRepository.save(account);

        RewardTransaction rewardTransaction = rewardTransactionRepository.findById(rewardTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("RewardTransaction not found with id: " + rewardTransactionId));

        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccount(account);
        transaction.setAmount(amount);
        transaction.setType(LoyaltyTransactionType.CREDIT);
        transaction.setDescription(description);
        transaction.setRewardTransaction(rewardTransaction);

        loyaltyTransactionRepository.save(transaction);
        log.info("Successfully credited {} points to user {}. New balance: {}", amount, userId, account.getBalance());
    }

    /**
     * Debits a specified amount of points from a user's loyalty account.
     * The operation is tenant-aware.
     *
     * @param userId The ID of the user to debit.
     * @param amount The amount of points to debit. Must be positive.
     * @param description A description of the debit transaction.
     * @throws IllegalArgumentException if the debit amount is not positive.
     * @throws EntityNotFoundException if the loyalty account is not found for the user and tenant.
     * @throws IllegalStateException if the user has insufficient balance.
     */
    @Transactional
    public void debitPoints(String userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to debit non-positive amount {} for user {}. Skipping.", amount, userId);
            throw new IllegalArgumentException("Debit amount must be positive.");
        }

        Long tenantId = userApi.getTenantId();
        LoyaltyAccount account = loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for user: " + userId + " and tenant: " + tenantId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance to debit " + amount + " points from user: " + userId);
        }

        account.setBalance(account.getBalance().subtract(amount));
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccount(account);
        transaction.setAmount(amount);
        transaction.setType(LoyaltyTransactionType.DEBIT);
        transaction.setDescription(description);
        transaction.setRewardTransaction(null);

        loyaltyTransactionRepository.save(transaction);
        log.info("Successfully debited {} points from user {}. New balance: {}", amount, userId, account.getBalance());
    }

    /**
     * Retrieves the current loyalty point balance for a specific user, scoped by tenant.
     *
     * @param userId The ID of the user.
     * @return The current balance of the loyalty account, or "BigDecimal.ZERO" if no account is found.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        Long tenantId = userApi.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Finds all loyalty transactions for a specific loyalty account.
     *
     * @param accountId The UUID of the loyalty account.
     * @return A list of {@link LoyaltyTransactionResponse} DTOs.
     * @throws EntityNotFoundException if the loyalty account is not found.
     */
    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> findTransactionsByAccountId(UUID accountId) {
        if (!loyaltyAccountRepository.existsById(accountId)) {
            throw new EntityNotFoundException("Loyalty account not found with id: " + accountId);
        }
        return loyaltyTransactionRepository.findByLoyaltyAccountId(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a {@link LoyaltyTransaction} entity to a {@link LoyaltyTransactionResponse} DTO.
     *
     * @param transaction The {@link LoyaltyTransaction} entity to map.
     * @return The corresponding {@link LoyaltyTransactionResponse} DTO.
     */
    private LoyaltyTransactionResponse mapToResponse(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getLoyaltyAccount().getId(),
                transaction.getRewardTransaction() != null ? transaction.getRewardTransaction().getId() : null,
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
