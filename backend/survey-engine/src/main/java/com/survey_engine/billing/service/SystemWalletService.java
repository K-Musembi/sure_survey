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

    @PostConstruct
    public void initWallets() {
        for (SystemWalletType type : SystemWalletType.values()) {
            if (systemWalletRepository.findByWalletType(type).isEmpty()) {
                SystemWallet wallet = new SystemWallet();
                wallet.setWalletType(type);
                wallet.setCurrentBalance(BigDecimal.ZERO);
                wallet.setReservedBalance(BigDecimal.ZERO);
                systemWalletRepository.save(wallet);
            }
        }
    }

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
        // This is a simplified simulation. In production, this would handle OAuth token generation,
        // request signing, and specific payloads for B2C or B2B API.
        Boolean apiSuccess = WebClient.create(safaricomApiUrl)
                .post()
                .bodyValue(String.format("{\"command\": \"PURCHASE\", \"type\": \"%s\", \"amount\": %s}", type, amount))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true) // Assume success for prototype
                .onErrorResume(e -> {
                    log.error("Failed to call Safaricom API: {}", e.getMessage());
                    return Mono.just(false); // In real app, throw or handle gracefully
                })
                .block(); // Blocking here because the service method is transactional and synchronous for the admin

        // In a real scenario, we might wait for a callback/IPN from Safaricom before crediting.
        // For this architecture, we assume immediate success or manual reconciliation.
        
        // 2. Credit System Wallet
        SystemWallet wallet = systemWalletRepository.findByWalletType(type)
                .orElseThrow(); // Should exist due to @PostConstruct

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