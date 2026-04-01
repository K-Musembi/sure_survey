package com.survey_engine.billing.service;

import com.survey_engine.billing.models.SystemWallet;
import com.survey_engine.billing.models.enums.SystemWalletType;
import com.survey_engine.billing.repository.SystemWalletRepository;
import com.survey_engine.billing.service.client.StockProvider;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ExternalServiceException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the System's inventory of digital assets (Airtime, Data).
 * Handles restocking from external providers and reserving stock for tenant rewards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemWalletService {

    private final SystemWalletRepository systemWalletRepository;
    private final Map<String, StockProvider> stockProviders;

    /**
     * Restocks the system inventory from an external provider.
     *
     * @param type   The type of asset to restock.
     * @param amount The amount to purchase.
     * @param providerName The name of the provider to use (e.g., "SAFARICOM", "CREDOFASTER").
     */
    @Transactional
    public void restockInventory(SystemWalletType type, BigDecimal amount, String providerName) {
        log.info("Initiating restock for {} with amount {} via {}", type, amount, providerName);

        StockProvider provider = stockProviders.get(providerName);
        if (provider == null) {
            throw new BusinessRuleException("UNKNOWN_STOCK_PROVIDER", "Unknown stock provider: " + providerName);
        }

        boolean success = provider.purchaseStock(type, amount);

        if (!success) {
            throw new ExternalServiceException("STOCK_PURCHASE_FAILED",
                    providerName + " API call failed. Restock aborted to prevent funds mismatch.", null);
        }

        // Credit System Wallet
        SystemWallet wallet = systemWalletRepository.findByWalletType(type)
                .orElseThrow(() -> new ResourceNotFoundException("SYSTEM_WALLET_NOT_FOUND", "System wallet not initialized for " + type));

        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        systemWalletRepository.save(wallet);
        log.info("Restocked {}. New Balance: {}", type, wallet.getCurrentBalance());
    }

    /**
     * Reserves stock for a tenant's reward campaign.
     * Decreases available balance, increases reserved balance.
     * Throws exception if insufficient stock.
     */
    @Transactional
    public void reserveStock(SystemWalletType type, BigDecimal amount) {
        SystemWallet wallet = systemWalletRepository.findByWalletType(type)
                .orElseThrow(() -> new ResourceNotFoundException("SYSTEM_WALLET_NOT_FOUND", "System wallet not initialized for " + type));

        BigDecimal available = wallet.getCurrentBalance().subtract(wallet.getReservedBalance());
        if (available.compareTo(amount) < 0) {
            throw new BusinessRuleException("INSUFFICIENT_SYSTEM_INVENTORY",
                    "Insufficient system inventory for " + type + ". Available: " + available + ", Required: " + amount);
        }

        wallet.setReservedBalance(wallet.getReservedBalance().add(amount));
        systemWalletRepository.save(wallet);
        log.info("Reserved {} units of {}. Total Reserved: {}", amount, type, wallet.getReservedBalance());
    }

    /**
     * Commits a reservation after successful disbursement.
     * Decreases both Current and Reserved balance.
     */
    @Transactional
    public void commitReservation(SystemWalletType type, BigDecimal amount) {
        SystemWallet wallet = systemWalletRepository.findByWalletType(type).orElseThrow();
        
        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount));
        wallet.setCurrentBalance(wallet.getCurrentBalance().subtract(amount));
        
        systemWalletRepository.save(wallet);
        log.info("Committed reservation for {}. Amount: {}", type, amount);
    }

    /**
     * Rolls back a reservation after failed disbursement.
     * Decreases Reserved balance only (making funds available again).
     */
    @Transactional
    public void rollbackReservation(SystemWalletType type, BigDecimal amount) {
        SystemWallet wallet = systemWalletRepository.findByWalletType(type).orElseThrow();

        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount));
        // Current balance remains unchanged, effectively freeing up the amount
        
        systemWalletRepository.save(wallet);
        log.info("Rolled back reservation for {}. Amount: {}", type, amount);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAvailableStock(SystemWalletType type) {
        return systemWalletRepository.findByWalletType(type)
                .map(w -> w.getCurrentBalance().subtract(w.getReservedBalance()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Returns status of all system wallets for admin dashboard.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllWalletStatus() {
        return systemWalletRepository.findAll().stream()
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("walletType", w.getWalletType().name());
                    map.put("currentBalance", w.getCurrentBalance());
                    map.put("reservedBalance", w.getReservedBalance());
                    map.put("availableBalance", w.getCurrentBalance().subtract(w.getReservedBalance()));
                    map.put("updatedAt", w.getUpdatedAt());
                    return map;
                })
                .toList();
    }
}
