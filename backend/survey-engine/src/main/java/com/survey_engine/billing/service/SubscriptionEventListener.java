package com.survey_engine.billing.service;

import com.survey_engine.common.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for user registration events and provisions billing infrastructure.
 * New users start on an implicit free tier (3 surveys, 25 responses, WEB only)
 * with no subscription record. A wallet is created so they can top up later
 * when they subscribe or need paid channels/rewards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventListener {

    private final WalletService walletService;

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for userId: {} tenantId: {}", event.userId(), event.tenantId());
        try {
            // Create wallet so user can top up when they subscribe or use paid features.
            // No subscription is created — user starts on the implicit free tier.
            walletService.getOrCreateWallet(event.tenantId(), event.userId());
            log.info("Wallet provisioned for user {} tenant {}", event.userId(), event.tenantId());
        } catch (Exception e) {
            log.error("Failed to provision wallet for user {}: {}", event.userId(), e.getMessage(), e);
        }
    }
}
