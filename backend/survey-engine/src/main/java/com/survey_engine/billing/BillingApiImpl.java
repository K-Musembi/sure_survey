package com.survey_engine.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.billing.models.Plan;
import com.survey_engine.billing.models.enums.SystemWalletType;
import com.survey_engine.billing.repository.PlanRepository;
import com.survey_engine.billing.service.InvoiceService;
import com.survey_engine.billing.service.SubscriptionLimitService;
import com.survey_engine.billing.service.SubscriptionService;
import com.survey_engine.billing.service.SystemWalletService;
import com.survey_engine.billing.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final WalletService walletService;
    private final SubscriptionLimitService subscriptionLimitService;
    private final SystemWalletService systemWalletService;
    private final PlanRepository planRepository;
    private final ObjectMapper objectMapper;

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

    @Override
    public void creditWallet(Long tenantId, BigDecimal amount, String reference, String description) {
        log.info("Crediting wallet for tenant {}: amount={}", tenantId, amount);
        walletService.creditWallet(tenantId, amount, reference, description);
    }

    @Override
    public void debitWallet(Long tenantId, BigDecimal amount, String description) {
        log.info("Debiting wallet for tenant {}: amount={}", tenantId, amount);
        walletService.debitWallet(tenantId, amount, null, description);
    }

    @Override
    public BigDecimal getWalletBalance(Long tenantId) {
        return walletService.getBalance(tenantId);
    }

    @Override
    public void validateSurveyCreationLimit(Long tenantId) {
        subscriptionLimitService.validateSurveyCreationLimit(tenantId);
    }

    @Override
    public void validateResponseLimit(Long tenantId, Long surveyId) {
        subscriptionLimitService.validateResponseLimit(tenantId, surveyId);
    }

    @Override
    public void updatePlan(Long planId, BigDecimal price, Map<String, Object> features) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (price != null) {
            plan.setPrice(price);
        }
        if (features != null) {
            try {
                String json = objectMapper.writeValueAsString(features);
                plan.setFeatures(json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize plan features", e);
            }
        }
        planRepository.save(plan);
    }

    @Override
    public void restockSystemWallet(String walletType, BigDecimal amount) {
        systemWalletService.restockInventory(SystemWalletType.valueOf(walletType), amount);
    }

    @Override
    public void reserveSystemStock(String walletType, BigDecimal amount) {
        systemWalletService.reserveStock(SystemWalletType.valueOf(walletType), amount);
    }

    @Override
    public void commitSystemReservation(String walletType, BigDecimal amount) {
        systemWalletService.commitReservation(SystemWalletType.valueOf(walletType), amount);
    }

    @Override
    public void rollbackSystemReservation(String walletType, BigDecimal amount) {
        systemWalletService.rollbackReservation(SystemWalletType.valueOf(walletType), amount);
    }
}
