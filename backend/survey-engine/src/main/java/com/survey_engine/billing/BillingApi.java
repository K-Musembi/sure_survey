package com.survey_engine.billing;

import org.springframework.modulith.NamedInterface;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Public API for the Billing module, exposing subscription and invoice management functionalities.
 * This interface allows other modules to interact with billing-related data without direct
 * dependencies on the internal implementation details of the 'billing' module.
 */
@NamedInterface
public interface BillingApi {

    /**
     * Creates a new subscription plan.
     * @param name The name of the plan.
     * @param price The price of the plan.
     * @param interval The billing interval (e.g., MONTHLY, YEARLY).
     * @param features A map of features included in the plan.
     * @return The ID of the created plan.
     */
    Long createPlan(String name, BigDecimal price, String interval, Map<String, Object> features);

    /**
     * Configures a payment gateway for a specific plan.
     * @param planId The ID of the plan.
     * @param gatewayType The type of gateway (e.g., "PAYSTACK").
     * @param gatewayCode The code used by the gateway to identify this plan.
     */
    void configurePlanGateway(Long planId, String gatewayType, String gatewayCode);

    /**
     * Handles incoming webhook events related to subscriptions from the payment gateway.
     * This method is responsible for processing events like subscription creation, updates, and cancellations.
     *
     * @param eventType The type of the webhook event (e.g., "subscription.created", "subscription.updated").
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    void handleSubscriptionWebhookEvent(String eventType, Map<String, Object> eventData);

    /**
     * Handles incoming webhook events related to invoices from the payment gateway.
     * This method is responsible for processing events like invoice creation, payment success, and payment failure.
     *
     * @param eventType The type of the webhook event (e.g., "invoice.created", "invoice.payment_succeeded").
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    void handleInvoiceWebhookEvent(String eventType, Map<String, Object> eventData);

    /**
     * Credits the tenant's wallet with a specified amount.
     *
     * @param tenantId The ID of the tenant.
     * @param amount The amount to credit.
     * @param reference A unique reference for the transaction (e.g., payment ID).
     * @param description A description of the transaction.
     */
    void creditWallet(Long tenantId, BigDecimal amount, String reference, String description);

    /**
     * Debits the tenant's wallet with a specified amount.
     *
     * @param tenantId The ID of the tenant.
     * @param amount The amount to debit.
     * @param description A description of the transaction.
     * @throws IllegalStateException if funds are insufficient.
     */
    void debitWallet(Long tenantId, BigDecimal amount, String description);

    /**
     * Retrieves the current balance of the tenant's wallet.
     *
     * @param tenantId The ID of the tenant.
     * @return The current balance.
     */
    BigDecimal getWalletBalance(Long tenantId);

    /**
     * Validates if the tenant is allowed to create a new survey based on their subscription.
     * @param tenantId The ID of the tenant.
     * @throws IllegalStateException if the limit is reached.
     */
    void validateSurveyCreationLimit(Long tenantId);

    /**
     * Validates if a survey is allowed to accept more responses based on the subscription.
     * @param tenantId The ID of the tenant.
     * @param surveyId The ID of the survey.
     * @throws IllegalStateException if the limit is reached.
     */
    void validateResponseLimit(Long tenantId, Long surveyId);

    /**
     * Updates a subscription plan's price and features.
     * @param planId The ID of the plan to update.
     * @param price The new price.
     * @param features The new features (as a parsed object or map, depending on implementation). 
     *                 Here we use Object to be generic, but implementation will likely expect a specific type or Map.
     *                 Actually, let's use the PlanFeatures record if it was shared, but user said no DTOs.
     *                 So we'll use Object (which will be the PlanFeatures record from the other module, but passed as Object? No, that's messy).
     *                 Let's use params: planId, price, features (JSON String maybe? or Map).
     *                 Let's use Map<String, Object> for features to be safe and standard.
     */
    void updatePlan(Long planId, BigDecimal price, Map<String, Object> features);

    /**
     * Restocks the system inventory from an external provider.
     * @param walletType The type of wallet to restock (e.g., "AIRTIME_STOCK", "DATA_BUNDLE_STOCK").
     * @param amount The amount to restock.
     */
    void restockSystemWallet(String walletType, BigDecimal amount);

    /**
     * Reserves stock from the system inventory.
     * @param walletType The type of wallet (e.g., "AIRTIME_STOCK", "DATA_BUNDLE_STOCK").
     * @param amount The amount to reserve.
     */
    void reserveSystemStock(String walletType, BigDecimal amount);

    /**
     * Commits a reservation from the system inventory.
     * @param walletType The type of wallet (e.g., "AIRTIME_STOCK", "DATA_BUNDLE_STOCK").
     * @param amount The amount to commit.
     */
    void commitSystemReservation(String walletType, BigDecimal amount);

    /**
     * Rolls back a reservation from the system inventory.
     * @param walletType The type of wallet (e.g., "AIRTIME_STOCK", "DATA_BUNDLE_STOCK").
     * @param amount The amount to rollback.
     */
    void rollbackSystemReservation(String walletType, BigDecimal amount);
}
