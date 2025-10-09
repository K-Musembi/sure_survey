package com.payments.payments.controller;

import com.payments.payments.dto.PaymentEventDetails;
import com.payments.payments.dto.PaymentEventRequest;
import com.payments.payments.dto.PaymentEventResponse;
import com.payments.payments.service.PaymentEventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller to handle payment initiation and retrieval.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
@Slf4j
@AllArgsConstructor
public class PaymentEventController {

    private final PaymentEventService paymentService;

    /**
     * Initializes a new payment.
     *
     * @param jwt            The authenticated user's JWT, injected by Spring Security.
     * @param paymentRequest The request body containing payment details.
     * @return A ResponseEntity containing the authorization URL for the frontend.
     */
    @PostMapping
    public ResponseEntity<PaymentEventResponse> initializePaymentEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentEventRequest paymentRequest) {

        String userId = jwt.getSubject();
        String userEmail = jwt.getClaimAsString("email");

        log.info("Initializing payment for user {} and survey {}", userId, paymentRequest.surveyId());
        PaymentEventResponse response = paymentService.createPaymentEvent(paymentRequest, userId, userEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all payments for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @return A list of payment details.
     */
    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentEventDetails>> getPaymentEvents(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Fetching all payments for user {}", userId);
        List<PaymentEventDetails> payments = paymentService.findPaymentEventsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Retrieves the details of a single payment by its ID.
     * Note: In a real-world scenario, you would add authorization here to ensure
     * the user requesting the payment is the one who owns it.
     *
     * @param id The UUID of the payment.
     * @return The payment details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentEventDetails> getPaymentEventById(@PathVariable UUID id) {
        log.info("Fetching payment details for paymentId {}", id);
        PaymentEventDetails payment = paymentService.findPaymentEventById(id);
        return ResponseEntity.ok(payment);
    }
}