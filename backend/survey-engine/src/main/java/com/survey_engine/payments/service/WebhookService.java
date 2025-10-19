package com.survey_engine.payments.service;

import com.survey_engine.common.events.PaymentSuccessEvent;
import com.survey_engine.payments.dto.paystack.PaystackWebhookData;
import com.survey_engine.payments.dto.paystack.PaystackWebhookEvent;
import com.survey_engine.payments.models.PaymentEvent;
import com.survey_engine.payments.models.Transaction;
import com.survey_engine.payments.models.enums.PaymentStatus;
import com.survey_engine.payments.models.enums.TransactionType;
import com.survey_engine.payments.repository.PaymentEventRepository;
import com.survey_engine.payments.repository.TransactionRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service class to handle the business logic for incoming webhooks.
 */
@Service
@Slf4j
@AllArgsConstructor
public class WebhookService {

    private final PaymentEventRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Processes a verified webhook event from PayStack.
     * This method is the core of the asynchronous payment confirmation process.
     *
     * @param event The deserialized payload from the PayStack webhook.
     */
    @Transactional
    public void processWebhookEvent(PaystackWebhookEvent<PaystackWebhookData> event) {
        log.info("Processing webhook event of type: {}", event.event());

        if ("charge.success".equals(event.event())) {
            handleChargeSuccess(event.data());
        } else {
            log.warn("Unhandled webhook event type: {}", event.event());
        }
    }

    /**
     * Handles the logic for a successful charge event.
     *
     * @param data The data object from the PayStack webhook.
     */
    private void handleChargeSuccess(PaystackWebhookData data) {
        String reference = data.reference();
        Long tenantId = TenantContext.getTenantId(); // Get tenantId from context
        PaymentEvent paymentEvent = paymentRepository.findByGatewayTransactionIdAndTenantId(reference, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("PaymentEvent with reference " + reference + " not found for tenant " + tenantId));

        // Idempotency Check: Ensure we haven't already processed this.
        if (paymentEvent.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Webhook for already succeeded paymentEvent received. Reference: {}. Ignoring.", reference);
            return;
        }

        paymentEvent.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(paymentEvent);
        Transaction transaction = createTransaction(paymentEvent, data);
        log.info("PaymentEvent {} SUCCEEDED. Created transaction {}.", paymentEvent.getId(), transaction.getId());

        // Publish an application event for other modules to consume
        PaymentSuccessEvent eventPayload = new PaymentSuccessEvent(
                paymentEvent.getId(),
                paymentEvent.getSurveyId(),
                paymentEvent.getUserId()
        );
        eventPublisher.publishEvent(eventPayload);
        log.info("Published PaymentSuccessEvent for surveyId: {}", paymentEvent.getSurveyId());
    }

    /**
     * Create a new Transaction instance and persist in the database
     * @param payment - Corresponding PaymentEvent instance
     * @param data - PayStack webhook data
     * @return - Transaction instance
     */
    private Transaction createTransaction(PaymentEvent payment, PaystackWebhookData data) {
        Transaction transaction = new Transaction();
        transaction.setPayment(payment);
        transaction.setType(TransactionType.CHARGE);
        // PayStack amount is in the smallest currency unit (long), convert back to major unit (BigDecimal).
        BigDecimal amount = new BigDecimal(data.amount()).divide(new BigDecimal(100), 2, RoundingMode.CEILING);
        transaction.setAmount(amount);
        transaction.setCurrency(data.currency());
        // PayStack's unique transaction ID is a long, convert to String for storage.
        transaction.setGatewayTransactionId(String.valueOf(data.transactionId()));
        transaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        return transaction;
    }
}