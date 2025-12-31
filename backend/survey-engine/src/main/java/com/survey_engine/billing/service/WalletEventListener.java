package com.survey_engine.billing.service;

import com.survey_engine.common.events.UserRegisteredEvent;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletEventListener {

    private final WalletService walletService;
    private final UserApi userApi;

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for wallet creation. TenantId: {}, UserId: {}", event.tenantId(), event.userId());
        try {
            // This will automatically create the appropriate wallet (User or Tenant) based on logic in WalletService
            walletService.getOrCreateWallet(event.tenantId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to create wallet for user {}", event.userId(), e);
        }
    }
}
