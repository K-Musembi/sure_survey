package com.survey_engine.billing.service;

import com.survey_engine.billing.dto.SubscriberInfo;
import com.survey_engine.billing.models.Plan;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.repository.PlanRepository;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        // For enterprise tenants, check if a subscription already exists.
        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        if (!tenantName.equals("Main Tenant")) {
            subscriptionRepository.findFirstByTenantIdAndStatusOrderByIdAsc(tenantId, SubscriptionStatus.ACTIVE)
                    .ifPresent(existingSub -> {
                        String creatorName = userApi.getUserNameById(String.valueOf(existingSub.getUserId()));
                        String createdAt = existingSub.getCreatedAt().format(DateTimeFormatter.ISO_DATE);
                        throw new IllegalStateException(String.format(
                                "Subscription for this enterprise client already created by %s on %s",
                                creatorName, createdAt));
                    });
        } else {
            // For individual users on the default tenant, check if they have a subscription.
            subscriptionRepository.findByTenantIdAndUserIdAndStatus(tenantId, userId, SubscriptionStatus.ACTIVE)
                    .ifPresent(s -> {
                        throw new IllegalStateException("You already have an active subscription.");
                    });
        }


        Map<String, String> userDetails = userApi.findUserDetailsMapById(String.valueOf(userId));
        if (userDetails.isEmpty()) {
            throw new EntityNotFoundException("User not found for ID: " + userId);
        }

        String name = userDetails.get("name");
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        String customerCode = paystackSubscriptionService.createCustomer(userDetails.get("email"), firstName, lastName, userDetails.get("phone"));
        var paystackSubscriptionData = paystackSubscriptionService.createSubscription(customerCode, plan.getPaystackPlanCode());

        if (paystackSubscriptionData == null) {
            throw new IllegalStateException("Failed to create subscription on Paystack, response was null.");
        }

        Subscription subscription = new Subscription();
        subscription.setTenantId(tenantId);
        subscription.setUserId(userId);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.TRIALING); // Start with trialing, webhook will confirm active status
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusDays(7)); // Example trial period
        subscription.setPaystackSubscriptionId(paystackSubscriptionData.subscriptionCode());
        subscription.setPaystackEmailToken(paystackSubscriptionData.emailToken());

        return subscriptionRepository.save(subscription);
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

        // An enterprise user can cancel their tenant's subscription.
        // An individual user can only cancel their own subscription.
        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        boolean isEnterprise = !tenantName.equals("Main Tenant");
        boolean hasPermission = isEnterprise ? subscription.getTenantId().equals(tenantId)
                                              : subscription.getUserId().equals(userId);

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission to cancel this subscription.");
        }

        paystackSubscriptionService.cancelSubscription(subscription.getPaystackSubscriptionId(), subscription.getPaystackEmailToken());

        subscription.setStatus(SubscriptionStatus.CANCELED);
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
        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        if (!tenantName.equals("Main Tenant")) {
            // For enterprise users, find the subscription by tenantId
            return subscriptionRepository.findFirstByTenantIdAndStatusOrderByIdAsc(tenantId, SubscriptionStatus.ACTIVE);
        } else {
            // For individual users, find the subscription by tenantId and userId
            return subscriptionRepository.findByTenantIdAndUserIdAndStatus(tenantId, userId, SubscriptionStatus.ACTIVE);
        }
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

        Subscription subscription = subscriptionRepository.findByPaystackSubscriptionId(paystackSubscriptionId)
                .orElseGet(() -> {
                    log.info("Creating new subscription from webhook for Paystack ID: {}", paystackSubscriptionId);
                    Subscription newSubscription = new Subscription();
                    newSubscription.setPaystackSubscriptionId(paystackSubscriptionId);
                    newSubscription.setTenantId(subscriberInfo.tenantId());
                    newSubscription.setUserId(subscriberInfo.userId());
                    Plan plan = planRepository.findByPaystackPlanCode(planCode)
                            .orElseThrow(() -> new EntityNotFoundException("Plan not found for Paystack plan code: " + planCode));
                    newSubscription.setPlan(plan);
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