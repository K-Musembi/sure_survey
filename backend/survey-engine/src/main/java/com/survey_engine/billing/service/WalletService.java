package com.survey_engine.billing.service;

import com.survey_engine.billing.models.TenantWallet;
import com.survey_engine.billing.models.WalletTransaction;
import com.survey_engine.billing.models.enums.WalletTransactionType;
import com.survey_engine.billing.repository.TenantWalletRepository;
import com.survey_engine.billing.repository.WalletTransactionRepository;
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

    private final TenantWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    /**
     * Ensures a wallet exists for the tenant.
     */
    @Transactional
    public TenantWallet getOrCreateWallet(Long tenantId) {
        return walletRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantWallet wallet = new TenantWallet();
                    wallet.setTenantId(tenantId);
                    wallet.setBalance(BigDecimal.ZERO);
                    wallet.setCurrency("KES"); // Defaulting to KES for now
                    return walletRepository.save(wallet);
                });
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long tenantId) {
        return getOrCreateWallet(tenantId).getBalance();
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(Long tenantId) {
        TenantWallet wallet = getOrCreateWallet(tenantId);
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
    public void creditWallet(Long tenantId, BigDecimal amount, String reference, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        // Lock the wallet row
        TenantWallet wallet = walletRepository.findByTenantIdAndIdIsNotNull(tenantId)
                .orElseGet(() -> getOrCreateWallet(tenantId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(WalletTransactionType.CREDIT);
        transaction.setReferenceId(reference);
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        log.info("Credited wallet for tenant {}: amount={}, newBalance={}", tenantId, amount, wallet.getBalance());
    }

    /**
     * Debits the wallet. Throws exception if insufficient funds.
     */
    @Transactional
    public void debitWallet(Long tenantId, BigDecimal amount, String reference, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        TenantWallet wallet = walletRepository.findByTenantIdAndIdIsNotNull(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found for tenant: " + tenantId));

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

        log.info("Debited wallet for tenant {}: amount={}, newBalance={}", tenantId, amount, wallet.getBalance());
    }
}