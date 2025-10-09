package com.payments.payments.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.payments.dto.paystack.PaystackWebhookData;
import com.payments.payments.dto.paystack.PaystackWebhookEvent;
import com.payments.payments.models.PaymentEvent;
import com.payments.payments.models.enums.PaymentStatus;
import com.payments.payments.models.Transaction;
import com.payments.payments.models.enums.TransactionType;
import com.payments.payments.repository.PaymentEventRepository;
import com.payments.payments.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service class to handle the business logic for incoming webhooks.
 */
@Service
@Slf4j
@AllArgsConstructor
public class WebhookService {

    private final PaymentEventRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // Define RabbitMQ constants for clarity
    private static final String PAYMENT_EXCHANGE = "payment.exchange";
    private static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";

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
        PaymentEvent payment = paymentRepository.findByGatewayTransactionId(reference)
                .orElseThrow(() -> new EntityNotFoundException("PaymentEvent with reference " + reference + " not found."));

        // Idempotency Check: Ensure we haven't already processed this.
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Webhook for already succeeded payment received. Reference: {}. Ignoring.", reference);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
        Transaction transaction = createTransaction(payment, data);
        log.info("PaymentEvent {} SUCCEEDED. Created transaction {}.", payment.getId(), transaction.getId());

        // Publish event to RabbitMQ for the survey service
        publishPaymentSuccessEvent(payment);
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

    /**
     * Publishes a message to RabbitMQ indicating a payment was successful.
     *
     * @param payment The successfully completed payment.
     */
    private void publishPaymentSuccessEvent(PaymentEvent payment) {
        try {
            Map<String, String> eventMessage = Map.of(
                    "paymentId", payment.getId().toString(),
                    "surveyId", payment.getSurveyId(),
                    "userId", payment.getUserId(),
                    "status", payment.getStatus().toString()
            );
            String jsonMessage = objectMapper.writeValueAsString(eventMessage);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_SUCCEEDED_ROUTING_KEY, jsonMessage);
            log.info("Published payment success event for surveyId: {}", payment.getSurveyId());
        } catch (Exception e) {
            log.error("Failed to publish payment success event for paymentId: {}. Error: {}", payment.getId(), e.getMessage());
            // Re-throw to roll back the database transaction, ensuring data consistency.
            throw new RuntimeException("Failed to publish payment success event.", e);
        }
    }
}