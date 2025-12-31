package com.survey_engine.billing.service;

import com.survey_engine.billing.models.Wallet;
import com.survey_engine.billing.models.WalletTransaction;
import com.survey_engine.billing.models.enums.WalletTransactionType;
import com.survey_engine.billing.repository.WalletRepository;
import com.survey_engine.billing.repository.WalletTransactionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import com.survey_engine.billing.dto.WalletTransactionResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserApi userApi;

    /**
     * Ensures a wallet exists for the tenant (Enterprise) or user (Main Tenant).
     */
    @Transactional
    public Wallet getOrCreateWallet(Long tenantId, Long userId) {
        boolean isMainTenant = isMainTenant(tenantId);

        if (isMainTenant && userId != null) {
            // Individual User Wallet
            return walletRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Wallet wallet = new Wallet();
                        wallet.setTenantId(tenantId);
                        wallet.setUserId(userId);
                        wallet.setBalance(BigDecimal.ZERO);
                        wallet.setCurrency("KES");
                        return walletRepository.save(wallet);
                    });
        } else {
            // Enterprise Tenant Wallet (Shared)
            return walletRepository.findByTenantIdAndUserIdIsNull(tenantId)
                    .orElseGet(() -> {
                        Wallet wallet = new Wallet();
                        wallet.setTenantId(tenantId);
                        // userId remains null
                        wallet.setBalance(BigDecimal.ZERO);
                        wallet.setCurrency("KES");
                        return walletRepository.save(wallet);
                    });
        }
    }
    
    // Fallback for legacy calls or pure tenant operations
    @Transactional
    public Wallet getOrCreateWallet(Long tenantId) {
        return getOrCreateWallet(tenantId, null);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long tenantId, Long userId) {
        return getOrCreateWallet(tenantId, userId).getBalance();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long tenantId) {
        return getBalance(tenantId, null);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(Long tenantId, Long userId) {
        Wallet wallet = getOrCreateWallet(tenantId, userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                .map(t -> new WalletTransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getType(),
                        t.getReferenceId(),
                        t.getDescription(),
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Credits the wallet. Safe for concurrent access via pessimistic locking.
     */
    @Transactional
    public void creditWallet(Long tenantId, Long userId, BigDecimal amount, String reference, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        Wallet wallet = resolveWalletWithLock(tenantId, userId);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(WalletTransactionType.CREDIT);
        transaction.setReferenceId(reference);
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        log.info("Credited wallet for tenant {} user {}: amount={}, newBalance={}", tenantId, userId, amount, wallet.getBalance());
    }

    /**
     * Debits the wallet. Throws exception if insufficient funds.
     */
    @Transactional
    public void debitWallet(Long tenantId, Long userId, BigDecimal amount, String reference, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        Wallet wallet = resolveWalletWithLock(tenantId, userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in wallet. Required: " + amount + ", Available: " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(WalletTransactionType.DEBIT);
        transaction.setReferenceId(reference);
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        log.info("Debited wallet for tenant {} user {}: amount={}, newBalance={}", tenantId, userId, amount, wallet.getBalance());
    }

    private Wallet resolveWalletWithLock(Long tenantId, Long userId) {
        boolean isMainTenant = isMainTenant(tenantId);
        
        if (isMainTenant && userId != null) {
             return walletRepository.findByUserIdAndIdIsNotNull(userId)
                    .orElseGet(() -> getOrCreateWallet(tenantId, userId));
        } else {
             return walletRepository.findByTenantIdAndUserIdIsNullAndIdIsNotNull(tenantId)
                    .orElseGet(() -> getOrCreateWallet(tenantId, userId));
        }
    }

    private boolean isMainTenant(Long tenantId) {
        return userApi.findTenantNameById(tenantId)
                .map("Main Tenant"::equals)
                .orElse(false);
    }

    /**
     * Migrates an individual user's wallet to be an enterprise tenant wallet.
     * This is used when a user upgrades to an enterprise account.
     *
     * @param userId The ID of the user.
     * @param newTenantId The ID of the new enterprise tenant.
     */
    @Transactional
    public void migrateWalletToEnterprise(Long userId, Long newTenantId) {
        // Find user's personal wallet
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User wallet not found for migration. UserId: " + userId));

        // Update to be a tenant wallet
        wallet.setUserId(null); // Clear user binding
        wallet.setTenantId(newTenantId); // Bind to new tenant

        walletRepository.save(wallet);
        log.info("Migrated wallet from user {} to new enterprise tenant {}", userId, newTenantId);
    }
}
