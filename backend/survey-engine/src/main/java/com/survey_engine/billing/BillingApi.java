package com.survey_engine.billing;

import org.springframework.modulith.NamedInterface;

import java.util.Map;

/**
 * Public API for the Billing module, exposing subscription and invoice management functionalities.
 * This interface allows other modules to interact with billing-related data without direct
 * dependencies on the internal implementation details of the 'billing' module.
 */
@NamedInterface
public interface BillingApi {

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
}
