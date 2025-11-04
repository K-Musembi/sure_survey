package com.survey_engine.billing.service;

import com.survey_engine.billing.models.Plan;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.repository.PlanRepository;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final WebhookTenantFinder webhookTenantFinder;

    /**
     * Creates a new subscription for a given tenant and plan.
     * This involves creating a customer in Paystack (if not exists), then creating a subscription
     * on Paystack's side, and finally persisting the local {@link Subscription} entity.
     *
     * @param tenantId The ID of the tenant for whom the subscription is being created.
     * @param planId The ID of the plan to subscribe to.
     * @return The newly created {@link Subscription} entity.
     * @throws EntityNotFoundException if the plan is not found.
     * @throws IllegalStateException if the tenant already has an active subscription.
     */
    @Transactional
    public Subscription createSubscription(Long tenantId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with ID: " + planId));

        // Check if tenant already has an active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository.findByTenantId(tenantId);
        if (existingSubscription.isPresent() && existingSubscription.get().getStatus() == SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Tenant already has an active subscription.");
        }

        // Get user details for customer creation
        Map<String, String> userDetails = userApi.findUserDetailsMapById(String.valueOf(tenantId));
        if (userDetails.isEmpty()) {
            throw new EntityNotFoundException("User not found for tenantId: " + tenantId);
        }

        String name = userDetails.get("name");
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create customer and then subscription in Paystack
        String customerCode = paystackSubscriptionService.createCustomer(userDetails.get("email"), firstName, lastName, userDetails.get("phone"));
        var paystackSubscriptionData = paystackSubscriptionService.createSubscription(customerCode, plan.getPaystackPlanCode());

        if (paystackSubscriptionData == null) {
            throw new IllegalStateException("Failed to create subscription on Paystack, response was null.");
        }

        Subscription subscription = new Subscription();
        subscription.setTenantId(tenantId);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1)); // Placeholder, will be updated by webhook
        subscription.setPaystackSubscriptionId(paystackSubscriptionData.subscriptionCode());
        subscription.setPaystackEmailToken(paystackSubscriptionData.emailToken());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Cancels an existing subscription for a given subscription ID.
     * This involves canceling the subscription in Paystack and updating the local status.
     *
     * @param subscriptionId The ID of the subscription to cancel.
     * @return The canceled {@link Subscription} entity.
     * @throws EntityNotFoundException if the subscription is not found.
     */
    @Transactional
    public Subscription cancelSubscription(UUID subscriptionId, Long tenantId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + subscriptionId));

        if (!subscription.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("You do not have permission to cancel this subscription.");
        }

        Boolean cancelledSuccessfully = paystackSubscriptionService.cancelSubscription(subscription.getPaystackSubscriptionId(), subscription.getPaystackEmailToken());
        log.info("Paystack subscription cancellation status for subscription {}: {}", subscriptionId, cancelledSuccessfully);

        subscription.setStatus(SubscriptionStatus.CANCELED);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Retrieves the current active subscription for a given tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return An {@link Optional} containing the active {@link Subscription} or empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscriptionForTenant(Long tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE);
    }

    /**
     * Handles various webhook events from the payment gateway related to subscriptions.
     * This method dispatches the event to appropriate handlers based on the event type.
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

        Long tenantId = webhookTenantFinder.findTenantId(eventData);

        Subscription subscription = subscriptionRepository.findByPaystackSubscriptionId(paystackSubscriptionId)
                .orElseGet(() -> {
                    log.info("Creating new subscription from webhook for Paystack ID: {}", paystackSubscriptionId);
                    Subscription newSubscription = new Subscription();
                    newSubscription.setPaystackSubscriptionId(paystackSubscriptionId);
                    newSubscription.setTenantId(tenantId);
                    Plan plan = planRepository.findByPaystackPlanCode(planCode)
                            .orElseThrow(() -> new EntityNotFoundException("Plan not found for Paystack plan code: " + planCode));
                    newSubscription.setPlan(plan);
                    return newSubscription;
                });

        // Update subscription status based on webhook event
        switch (status) {
            case "active":
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                break;
            case "cancelled":
                subscription.setStatus(SubscriptionStatus.CANCELED);
                break;
            case "non-renewing":
                subscription.setStatus(SubscriptionStatus.CANCELED); // Or a specific NON_RENEWING status
                break;
            case "past_due":
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                break;
            default:
                log.warn("Unknown subscription status from webhook: {}", status);
        }

        // Update period dates if available
        String nextPaymentDateStr = (String) eventData.get("next_payment_date");
        if (nextPaymentDateStr != null) {
            try {
                // Paystack typically returns dates in ISO 8601 format (e.g., "2023-10-27T10:00:00.000Z")
                // Adjust formatter if Paystack uses a different format
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime nextPaymentDate = LocalDateTime.parse(nextPaymentDateStr, formatter);
                subscription.setCurrentPeriodEnd(nextPaymentDate);
                // Assuming currentPeriodStart is the previous currentPeriodEnd or derived from nextPaymentDate
                // For simplicity, we'll just update currentPeriodEnd for now.
            } catch (Exception e) {
                log.error("Error parsing next_payment_date from webhook: {}", nextPaymentDateStr, e);
            }
        }

        subscriptionRepository.save(subscription);
        log.info("Subscription {} updated to status {}", subscription.getId(), subscription.getStatus());
    }
}