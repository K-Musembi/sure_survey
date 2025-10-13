package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.LoyaltyTransactionResponse;
import com.survey_engine.rewards.models.LoyaltyAccount;
import com.survey_engine.rewards.models.LoyaltyTransaction;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.LoyaltyTransactionType;
import com.survey_engine.rewards.repository.LoyaltyAccountRepository;
import com.survey_engine.rewards.repository.LoyaltyTransactionRepository;
import com.survey_engine.rewards.repository.RewardTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyTransactionService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final LoyaltyAccountService loyaltyAccountService; // Injected service

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

    @Transactional
    public void debitPoints(String userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to debit non-positive amount {} for user {}. Skipping.", amount, userId);
            throw new IllegalArgumentException("Debit amount must be positive.");
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for user: " + userId));

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

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> findTransactionsByAccountId(UUID accountId) {
        if (!loyaltyAccountRepository.existsById(accountId)) {
            throw new EntityNotFoundException("Loyalty account not found with id: " + accountId);
        }
        return loyaltyTransactionRepository.findByLoyaltyAccountId(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

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