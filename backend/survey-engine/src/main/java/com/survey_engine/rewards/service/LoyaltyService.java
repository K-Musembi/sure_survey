package com.survey_engine.rewards.service;

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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final RewardTransactionRepository rewardTransactionRepository;

    @Transactional
    public void creditPoints(String userId, BigDecimal amount, String description, UUID rewardTransactionId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to credit non-positive amount {} for user {}. Skipping.", amount, userId);
            return;
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> createNewAccount(userId));

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

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private LoyaltyAccount createNewAccount(String userId) {
        log.info("No loyalty account found for user {}. Creating a new one.", userId);
        LoyaltyAccount newAccount = new LoyaltyAccount();
        newAccount.setUserId(userId);
        newAccount.setBalance(BigDecimal.ZERO);
        return loyaltyAccountRepository.save(newAccount);
    }
}
