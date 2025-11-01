package com.survey_engine.billing.service;

import com.survey_engine.billing.models.Invoice;
import com.survey_engine.billing.models.enums.InvoiceStatus;
import com.survey_engine.billing.repository.InvoiceRepository;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service class for managing {@link Invoice} entities and handling related business logic.
 * This service processes webhook events from the payment gateway to update invoice statuses
 * and provides methods for retrieving invoice information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserApi userApi;
    private final WebhookTenantFinder webhookTenantFinder;

    /**
     * Finds an invoice by its ID for the current tenant.
     *
     * @param invoiceId The ID of the invoice to find.
     * @return The {@link Invoice} entity.
     * @throws EntityNotFoundException if the invoice is not found or does not belong to the current tenant.
     */
    @Transactional(readOnly = true)
    public Invoice findInvoiceById(UUID invoiceId) {
        Long tenantId = userApi.getTenantId();
        return invoiceRepository.findById(invoiceId)
                .filter(invoice -> invoice.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + invoiceId));
    }

    /**
     * Finds all invoices for a given tenant ID.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of {@link Invoice} entities.
     */
    @Transactional(readOnly = true)
    public List<Invoice> findInvoicesByTenantId(Long tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    /**
     * Handles various webhook events from the payment gateway related to invoices.
     * This method dispatches the event to appropriate handlers based on the event type.
     *
     * @param eventType The type of the webhook event.
     * @param eventData A map containing the parsed data from the webhook payload.
     */
    @Transactional
    public void handleWebhookEvent(String eventType, Map<String, Object> eventData) {
        switch (eventType) {
            case "invoice.create":
            case "invoice.update":
            case "invoice.payment_succeeded":
            case "invoice.payment_failed":
                handlePaystackInvoiceEvent(eventType, eventData);
                break;
            default:
                log.warn("Unhandled invoice webhook event type: {}", eventType);
        }
    }

    /**
     * Processes Paystack invoice-related webhook events.
     *
     * @param eventType The type of the Paystack invoice event.
     * @param eventData The data payload of the event.
     */
    private void handlePaystackInvoiceEvent(String eventType, Map<String, Object> eventData) {
        String paystackInvoiceId = (String) eventData.get("invoice_code");
        String status = (String) eventData.get("status");
        String subscriptionCode = (String) eventData.get("subscription_code");

        Long tenantId = webhookTenantFinder.findTenantId(eventData);

        Invoice invoice = invoiceRepository.findByPaystackInvoiceId(paystackInvoiceId)
                .orElseGet(() -> {
                    log.info("Creating new invoice from webhook for Paystack ID: {}", paystackInvoiceId);
                    Invoice newInvoice = new Invoice();
                    newInvoice.setPaystackInvoiceId(paystackInvoiceId);
                    newInvoice.setTenantId(tenantId);
                    newInvoice.setAmount(extractAmountFromWebhook(eventData));

                    String dueDateStr = (String) eventData.get("due_date");
                    if (dueDateStr != null) {
                        try {
                            newInvoice.setDueDate(LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME));
                        } catch (Exception e) {
                            log.error("Error parsing due_date from webhook: {}", dueDateStr, e);
                        }
                    }

                    String invoicePdfUrl = (String) eventData.get("invoice_url");
                    newInvoice.setInvoicePdfUrl(invoicePdfUrl);

                    if (subscriptionCode != null) {
                        subscriptionRepository.findByPaystackSubscriptionId(subscriptionCode)
                                .ifPresent(newInvoice::setSubscription);
                    }
                    return newInvoice;
                });

        // Update invoice status based on webhook event
        switch (status) {
            case "draft":
                invoice.setStatus(InvoiceStatus.DRAFT);
                break;
            case "open":
                invoice.setStatus(InvoiceStatus.OPEN);
                break;
            case "paid":
                invoice.setStatus(InvoiceStatus.PAID);
                invoice.setPaidAt(LocalDateTime.now());
                break;
            case "failed":
                invoice.setStatus(InvoiceStatus.FAILED);
                break;
            case "void":
                invoice.setStatus(InvoiceStatus.VOID);
                break;
            default:
                log.warn("Unknown invoice status from webhook: {}", status);
        }

        invoiceRepository.save(invoice);
        log.info("Invoice {} updated to status {}", invoice.getId(), invoice.getStatus());
    }

    /**
     * Extracts and parses the amount from the webhook event data.
     *
     * @param eventData The data payload of the event.
     * @return The parsed amount as a BigDecimal, or BigDecimal ZERO if parsing fails.
     */
    private BigDecimal extractAmountFromWebhook(Map<String, Object> eventData) {
        Object amountObj = eventData.get("amount");
        if (amountObj instanceof Number) {
            return BigDecimal.valueOf(((Number) amountObj).doubleValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (amountObj instanceof String) {
            try {
                return new BigDecimal((String) amountObj).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                log.error("Error parsing amount from webhook: {}", amountObj, e);
                return BigDecimal.ZERO;
            }
        } else {
            log.warn("Amount in webhook is not a recognized type: {}", amountObj);
            return BigDecimal.ZERO;
        }
    }
}