package com.survey_engine.billing.service;

import com.survey_engine.billing.models.SystemWallet;
import com.survey_engine.billing.models.enums.SystemWalletType;
import com.survey_engine.billing.repository.SystemWalletRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Manages the System's inventory of digital assets (Airtime, Data).
 * Handles restocking from external providers (Safaricom) and reserving stock for tenant rewards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemWalletService {

    private final SystemWalletRepository systemWalletRepository;
    private final WebClient.Builder webClientBuilder;

    // Placeholder for Safaricom API URL - strictly for the refill logic
    @Value("${safaricom.api.url}")
    private String safaricomApiUrl;

    /**
     * Simulates an API call to Safaricom to purchase bulk airtime or data,
     * then credits the System Wallet.
     *
     * @param type   The type of asset to restock.
     * @param amount The amount to purchase (value in KES for Airtime, MBs for Data).
     */
    @Transactional
    public void restockInventory(SystemWalletType type, BigDecimal amount) {
        log.info("Initiating restock for {} with amount {}", type, amount);

        // 1. Perform External API Call (WebFlux)
        // Using the builder ensures we inherit any global configuration (logging, timeouts, etc.)
        Boolean apiSuccess = webClientBuilder.baseUrl(safaricomApiUrl).build()
                .post()
                .bodyValue(String.format("{\"command\": \"PURCHASE\", \"type\": \"%s\", \"amount\": %s}", type, amount))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true) // Assume success if we get a response body
                .onErrorResume(e -> {
                    log.error("Failed to call Safaricom API: {}", e.getMessage());
                    return Mono.just(false);
                })
                .block(); // Blocking here is acceptable for an administrative, low-throughput operation

        if (Boolean.FALSE.equals(apiSuccess)) {
            throw new RuntimeException("Safaricom API call failed. Restock aborted to prevent funds mismatch.");
        }

        // 2. Credit System Wallet
        // findByWalletType uses a PESSIMISTIC_WRITE lock, ensuring thread safety during the update
        SystemWallet wallet = systemWalletRepository.findByWalletType(type)
                .orElseThrow(() -> new IllegalStateException("System wallet not initialized for " + type));

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
                .orElseThrow(() -> new IllegalStateException("System wallet not initialized for " + type));

        BigDecimal available = wallet.getCurrentBalance().subtract(wallet.getReservedBalance());
        if (available.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient system inventory for " + type + ". Available: " + available + ", Required: " + amount);
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
}