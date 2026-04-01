package com.survey_engine.billing.service;

import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service that expires subscriptions whose billing period has ended
 * and have not been renewed via webhook. Runs every hour.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserApi userApi;

    /**
     * Checks for active subscriptions whose currentPeriodEnd has passed
     * and transitions them to EXPIRED. Unlinks from tenant/user.
     * Runs every hour at minute 30.
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void expireOverdueSubscriptions() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<Subscription> expired = subscriptionRepository
                    .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.ACTIVE, now);

            if (expired.isEmpty()) return;

            log.info("Found {} expired subscriptions to process", expired.size());

            for (Subscription sub : expired) {
                sub.setStatus(SubscriptionStatus.EXPIRED);

                // Unlink subscription from tenant/user so limit checks fall back to free tier
                if (sub.getUserId() != null) {
                    try {
                        userApi.updateUserSubscriptionId(sub.getUserId(), null);
                    } catch (Exception e) {
                        log.warn("Failed to unlink user subscription for userId={}: {}", sub.getUserId(), e.getMessage());
                    }
                }
                if (sub.getTenantId() != null) {
                    try {
                        userApi.updateTenantSubscriptionId(sub.getTenantId(), null);
                    } catch (Exception e) {
                        log.warn("Failed to unlink tenant subscription for tenantId={}: {}", sub.getTenantId(), e.getMessage());
                    }
                }

                subscriptionRepository.save(sub);
                log.info("Expired subscription {} for tenant={} user={}", sub.getId(), sub.getTenantId(), sub.getUserId());
            }
        } catch (Exception e) {
            log.error("Subscription expiry job failed: {}", e.getMessage(), e);
        }
    }
}
