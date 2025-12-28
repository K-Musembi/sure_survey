package com.survey_engine.billing.service;

import com.survey_engine.billing.dto.SubscriberInfo;
import com.survey_engine.billing.models.Plan;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.PlanGatewayMapping;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.models.enums.PaymentGatewayType;
import com.survey_engine.billing.repository.PlanRepository;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.billing.repository.PlanGatewayMappingRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing {@link Subscription} entities and handling related business logic.
 * This service interacts with the payment gateway (Paystack) for subscription lifecycle management
 * and updates the local database accordingly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanGatewayMappingRepository planGatewayMappingRepository;
    private final UserApi userApi;
    private final PaystackSubscriptionService paystackSubscriptionService;
    private final WebhookSubscriberFinder webhookSubscriberFinder;

    /**
     * Creates a new subscription for a given user and plan.
     *
     * @param tenantId The ID of the tenant for whom the subscription is being created.
     * @param userId The ID of the user creating the subscription.
     * @param planId The ID of the plan to subscribe to.
     * @return The newly created {@link Subscription} entity.
     * @throws EntityNotFoundException if the plan or user is not found.
     * @throws IllegalStateException if an active subscription already exists where one is not allowed.
     */
    @Transactional
    public Subscription createSubscription(Long tenantId, Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with ID: " + planId));

        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));
        
        boolean isEnterprise = !tenantName.equals("Main Tenant");
        
        // Check for existing subscription
        if (isEnterprise) {
            userApi.getTenantSubscriptionId(tenantId).ifPresent(subId -> {
                 Subscription existing = subscriptionRepository.findById(subId).orElse(null);
                 if (existing != null && existing.getStatus() == SubscriptionStatus.ACTIVE) {
                     throw new IllegalStateException("Tenant already has an active subscription.");
                 }
            });
        } else {
             userApi.getUserSubscriptionId(userId).ifPresent(subId -> {
                 Subscription existing = subscriptionRepository.findById(subId).orElse(null);
                 if (existing != null && existing.getStatus() == SubscriptionStatus.ACTIVE) {
                     throw new IllegalStateException("User already has an active subscription.");
                 }
             });
        }

        Map<String, String> userDetails = userApi.findUserDetailsMapById(String.valueOf(userId));
        if (userDetails.isEmpty()) {
            throw new EntityNotFoundException("User not found for ID: " + userId);
        }

        String gatewaySubscriptionId = null;
        String gatewayEmailToken = null;
        PaymentGatewayType gatewayType = null;
        SubscriptionStatus initialStatus = SubscriptionStatus.ACTIVE; // Default for free/internal plans
        
        // Check if there is a Paystack mapping (or other gateway) for this plan
        Optional<PlanGatewayMapping> gatewayMapping = planGatewayMappingRepository
                .findByPlanIdAndGatewayType(planId, PaymentGatewayType.PAYSTACK);

        if (gatewayMapping.isPresent()) {
            gatewayType = PaymentGatewayType.PAYSTACK;
            String name = userDetails.get("name");
            String[] nameParts = name.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            String customerCode = paystackSubscriptionService.createCustomer(userDetails.get("email"), firstName, lastName, userDetails.get("phone"));
            var paystackSubscriptionData = paystackSubscriptionService.createSubscription(customerCode, gatewayMapping.get().getGatewayPlanCode());

            if (paystackSubscriptionData == null) {
                throw new IllegalStateException("Failed to create subscription on Paystack, response was null.");
            }
            gatewaySubscriptionId = paystackSubscriptionData.subscriptionCode();
            gatewayEmailToken = paystackSubscriptionData.emailToken();
            initialStatus = SubscriptionStatus.TRIALING; // Wait for webhook to confirm
        } else {
            // No Paystack mapping. If price > 0, we can't process it (unless we support other gateways).
            if (plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                 throw new IllegalStateException("This paid plan is not configured for online payment. Please contact support.");
            }
        }

        Subscription subscription = new Subscription();
        subscription.setTenantId(tenantId);
        subscription.setUserId(userId);
        subscription.setPlan(plan);
        subscription.setStatus(initialStatus);
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusDays(30)); 
        subscription.setGatewaySubscriptionId(gatewaySubscriptionId);
        subscription.setGatewayEmailToken(gatewayEmailToken);
        subscription.setGatewayType(gatewayType);

        subscription = subscriptionRepository.save(subscription);

        // Link to User or Tenant
        if (isEnterprise) {
            userApi.updateTenantSubscriptionId(tenantId, subscription.getId());
        } else {
            userApi.updateUserSubscriptionId(userId, subscription.getId());
        }

        return subscription;
    }

    /**
     * Cancels an existing subscription.
     *
     * @param subscriptionId The ID of the subscription to cancel.
     * @param tenantId The tenant ID of the user requesting cancellation.
     * @param userId The user ID of the user requesting cancellation.
     * @return The canceled {@link Subscription} entity.
     */
    @Transactional
    public Subscription cancelSubscription(UUID subscriptionId, Long tenantId, Long userId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + subscriptionId));

        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));
        
        boolean isEnterprise = !tenantName.equals("Main Tenant");
        boolean hasPermission = isEnterprise ? subscription.getTenantId().equals(tenantId)
                                              : subscription.getUserId().equals(userId);

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission to cancel this subscription.");
        }

        if (subscription.getGatewaySubscriptionId() != null && subscription.getGatewayType() == PaymentGatewayType.PAYSTACK) {
            paystackSubscriptionService.cancelSubscription(subscription.getGatewaySubscriptionId(), subscription.getGatewayEmailToken());
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        
        // Unlink
        if (isEnterprise) {
            userApi.updateTenantSubscriptionId(tenantId, null);
        } else {
            userApi.updateUserSubscriptionId(userId, null);
        }

        return subscriptionRepository.save(subscription);
    }

    /**
     * Retrieves the active subscription for a user.
     *
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @return An {@link Optional} containing the active {@link Subscription} or empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscriptionForUser(Long tenantId, Long userId) {
        // 1. Check if User has a subscription (Individual)
        Optional<UUID> userSubId = userApi.getUserSubscriptionId(userId);
        if (userSubId.isPresent()) {
            return subscriptionRepository.findById(userSubId.get());
        }

        // 2. Check if Tenant has a subscription (Enterprise)
        Optional<UUID> tenantSubId = userApi.getTenantSubscriptionId(tenantId);
        if (tenantSubId.isPresent()) {
            return subscriptionRepository.findById(tenantSubId.get());
        }

        return Optional.empty();
    }

    /**
     * Changes the plan of an active subscription.
     *
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @param newPlanId The ID of the new plan.
     * @return The updated {@link Subscription} entity.
     */
    @Transactional
    public Subscription changePlan(Long tenantId, Long userId, Long newPlanId) {
        Subscription subscription = getActiveSubscriptionForUser(tenantId, userId)
                .orElseThrow(() -> new IllegalStateException("No active subscription found to change."));

        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with ID: " + newPlanId));

        // Logic to handle gateway update would go here if we were fully integrated.
        // For now, we update the local record.
        subscription.setPlan(newPlan);
        
        // If upgrading to a paid plan from free, we might need to trigger payment flow.
        // For this refactor, we assume the user has handled payment or it's an internal update.
        
        return subscriptionRepository.save(subscription);
    }

    /**
     * Retrieves all available subscription plans.
     * @return List of all plans.
     */
    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    /**
     * Handles various webhook events from the payment gateway related to subscriptions.
     *
     * @param eventType The type of the webhook event.
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    @Transactional
    public void handleWebhookEvent(String eventType, Map<String, Object> eventData) {
        switch (eventType) {
            case "subscription.create":
            case "subscription.not_renewing":
            case "subscription.update":
            case "subscription.disable":
            case "subscription.enable":
                handlePaystackSubscriptionEvent(eventType, eventData);
                break;
            default:
                log.warn("Unhandled subscription webhook event type: {}", eventType);
        }
    }

    /**
     * Processes Paystack subscription-related webhook events.
     *
     * @param eventType The type of the Paystack subscription event.
     * @param eventData The data payload of the event.
     */
    private void handlePaystackSubscriptionEvent(String eventType, Map<String, Object> eventData) {
        String paystackSubscriptionId = (String) eventData.get("subscription_code");
        String status = (String) eventData.get("status");
        String planCode = (String) eventData.get("plan_code");

        SubscriberInfo subscriberInfo = webhookSubscriberFinder.findSubscriber(eventData);

        Subscription subscription = subscriptionRepository.findByGatewaySubscriptionId(paystackSubscriptionId)
                .orElseGet(() -> {
                    log.info("Creating new subscription from webhook for Paystack ID: {}", paystackSubscriptionId);
                    Subscription newSubscription = new Subscription();
                    newSubscription.setGatewaySubscriptionId(paystackSubscriptionId);
                    newSubscription.setGatewayType(PaymentGatewayType.PAYSTACK);
                    newSubscription.setTenantId(subscriberInfo.tenantId());
                    newSubscription.setUserId(subscriberInfo.userId());
                    
                    // Resolve Plan via Mapping
                    PlanGatewayMapping mapping = planGatewayMappingRepository.findByGatewayPlanCodeAndGatewayType(planCode, PaymentGatewayType.PAYSTACK)
                            .orElseThrow(() -> new EntityNotFoundException("Plan mapping not found for Paystack code: " + planCode));
                    
                    newSubscription.setPlan(mapping.getPlan());
                    return newSubscription;
                });

        switch (status) {
            case "active":
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                break;
            case "cancelled":
            case "non-renewing":
                subscription.setStatus(SubscriptionStatus.CANCELED);
                break;
            case "past_due":
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                break;
            default:
                log.warn("Unknown subscription status from webhook: {}", status);
        }

        String nextPaymentDateStr = (String) eventData.get("next_payment_date");
        if (nextPaymentDateStr != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime nextPaymentDate = LocalDateTime.parse(nextPaymentDateStr, formatter);
                subscription.setCurrentPeriodEnd(nextPaymentDate);
            } catch (Exception e) {
                log.error("Error parsing next_payment_date from webhook: {}", nextPaymentDateStr, e);
            }
        }

        subscriptionRepository.save(subscription);
        log.info("Subscription {} updated to status {}", subscription.getId(), subscription.getStatus());
    }
}