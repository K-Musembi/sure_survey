package com.survey_engine.billing;

import com.survey_engine.billing.service.InvoiceService;
import com.survey_engine.billing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementation of the {@link BillingApi} interface.
 * This class serves as the entry point for other modules to interact with the billing functionalities,
 * delegating calls to specific services within the billing module.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingApiImpl implements BillingApi {

    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;

    /**
     * Handles incoming webhook events related to subscriptions from the payment gateway.
     * Delegates the event processing to the {@link SubscriptionService}.
     *
     * @param eventType The type of the webhook event (e.g., "subscription.created", "subscription.updated").
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    @Override
    public void handleSubscriptionWebhookEvent(String eventType, Map<String, Object> eventData) {
        log.info("Handling subscription webhook event: {} with data: {}", eventType, eventData);
        // Delegate to SubscriptionService to handle the specific event type
        subscriptionService.handleWebhookEvent(eventType, eventData);
    }

    /**
     * Handles incoming webhook events related to invoices from the payment gateway.
     * Delegates the event processing to the {@link InvoiceService}.
     *
     * @param eventType The type of the webhook event (e.g., "invoice.created", "invoice.payment_succeeded").
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    @Override
    public void handleInvoiceWebhookEvent(String eventType, Map<String, Object> eventData) {
        log.info("Handling invoice webhook event: {} with data: {}", eventType, eventData);
        // Delegate to InvoiceService to handle the specific event type
        invoiceService.handleWebhookEvent(eventType, eventData);
    }
}
