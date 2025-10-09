package com.payments.payments.service;

import com.payments.payments.dto.PaymentEventDetails;
import com.payments.payments.dto.PaymentEventRequest;
import com.payments.payments.dto.PaymentEventResponse;
import com.payments.payments.dto.paystack.PaystackResponse;
import com.payments.payments.models.PaymentEvent;
import com.payments.payments.models.enums.PaymentGateway;
import com.payments.payments.models.enums.PaymentStatus;
import com.payments.payments.repository.PaymentEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing the payment lifecycle.
 * This service orchestrates the process of creating and tracking a payment attempt.
 */
@Service
@Slf4j
@AllArgsConstructor
public class PaymentEventService {

    private final PaymentEventRepository paymentRepository;
    private final PaystackService paystackService;


    /**
     * Creates a new payment attempt.
     *
     * @param request   The payment request details.
     * @param userId    The ID of the user initiating the payment.
     * @param userEmail The email of the user, required by PayStack.
     * @return A PaymentEventResponse containing the authorization URL for the frontend.
     */
    @Transactional
    public PaymentEventResponse createPaymentEvent(PaymentEventRequest request, String userId, String userEmail) {
        // Idempotency Check: See if a paymentEvent with this key already exists.
        paymentRepository.findByIdempotencyKey(request.idempotencyKey()).ifPresent(payment -> {
            log.warn("Idempotency key conflict: {}", request.idempotencyKey());
            throw new DataIntegrityViolationException("A paymentEvent with this idempotency key already exists.");
        });

        // Initialize paymentEvent with PayStack
        String reference = UUID.randomUUID().toString();
        PaystackResponse paystackResponse = paystackService.initializePayment(request, userEmail, reference)
                .block(); // Block to get the result in this transactional context

        if (paystackResponse == null || !paystackResponse.status()) {
            log.error("PayStack initialization failed: {}", paystackResponse != null ? paystackResponse.message() : "No response");
            throw new IllegalStateException("PaymentEvent gateway failed to initialize transaction.");
        }

        // Create and save our internal PaymentEvent entity
        PaymentEvent paymentEvent = persistPaymentEvent(request, paystackResponse, userId, userEmail);
        log.info("Successfully created and saved pending paymentEvent with reference: {}", paymentEvent.getGatewayTransactionId());

        // Return the authorization URL to the controller
        return new PaymentEventResponse(
                paystackResponse.data().authorizationUrl(),
                paystackResponse.data().accessCode(),
                paystackResponse.data().reference()
        );
    }

    /**
     * Finds a payment by its internal ID.
     *
     * @param id The UUID of the payment.
     * @return The found PaymentEvent entity.
     * @throws EntityNotFoundException if no payment is found.
     */
    @Transactional(readOnly = true)
    public PaymentEventDetails findPaymentEventById(UUID id) {
        return paymentRepository.findById(id)
                .map(this::mapToPaymentEventDetails)
                .orElseThrow(() -> new EntityNotFoundException("PaymentEvent with ID " + id + " not found."));
    }

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of DTOs with payment details.
     */
    @Transactional(readOnly = true)
    public List<PaymentEventDetails> findPaymentEventsByUserId(String userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToPaymentEventDetails)
                .collect(Collectors.toList());
    }

    /**
     * Create PaymentEvent instance and persist to database
     * @param request - PaymentEventRequest instance
     * @param paystackResponse - PaystackResponse instance
     * @param userId - user id
     * @param userEmail - user email
     * @return The persisted PaymentEvent entity.
     */
    private PaymentEvent persistPaymentEvent(
            PaymentEventRequest request, PaystackResponse paystackResponse, String userId, String userEmail) {

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setUserId(userId);
        paymentEvent.setEmail(userEmail);
        paymentEvent.setSurveyId(request.surveyId());
        paymentEvent.setIdempotencyKey(request.idempotencyKey());
        paymentEvent.setAmount(request.amount());
        paymentEvent.setCurrency(request.currency());
        paymentEvent.setStatus(PaymentStatus.PENDING);
        paymentEvent.setPaymentGateway(PaymentGateway.PAYSTACK);
        paymentEvent.setGatewayTransactionId(paystackResponse.data().reference()); // This is the crucial link

        return paymentRepository.save(paymentEvent);
    }

    /**
     * Maps a PaymentEvent entity to a PaymentEventDetails DTO.
     *
     * @param paymentEvent The entity to map.
     * @return The mapped DTO.
     */
    private PaymentEventDetails mapToPaymentEventDetails(PaymentEvent paymentEvent) {
        return new PaymentEventDetails(
                paymentEvent.getId(),
                paymentEvent.getSurveyId(),
                paymentEvent.getAmount(),
                paymentEvent.getCurrency(),
                paymentEvent.getStatus(),
                paymentEvent.getGatewayTransactionId(),
                paymentEvent.getCreatedAt()
        );
    }
}