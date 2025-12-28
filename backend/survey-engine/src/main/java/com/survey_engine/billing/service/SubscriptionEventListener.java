package com.survey_engine.billing.service;

import com.survey_engine.billing.models.Plan;
import com.survey_engine.billing.repository.PlanRepository;
import com.survey_engine.common.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventListener {

    private final SubscriptionService subscriptionService;
    private final PlanRepository planRepository;

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for userId: {}", event.userId());
        try {
            Long freePlanId = getFreePlanId();
            // We attempt to create a subscription. 
            // The service logic already checks if one exists.
            // For Enterprise, if the tenant already has one (created by another admin?), it throws/skips.
            // For Individual, it checks the user.
            
            // We just need to handle the "already exists" gracefully here.
            try {
                subscriptionService.createSubscription(event.tenantId(), event.userId(), freePlanId);
            } catch (IllegalStateException e) {
                // Ignore if subscription already exists
                log.info("Subscription already exists or not required: {}", e.getMessage());
            }
        } catch (Exception e) {
             log.error("Failed to create default subscription for user {}", event.userId(), e);
        }
    }

    private Long getFreePlanId() {
        return planRepository.findByName("FREE")
                .map(Plan::getId)
                .orElseThrow(() -> new IllegalStateException("FREE plan not found in database."));
    }
}
