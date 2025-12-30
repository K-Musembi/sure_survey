package com.survey_engine.payments.controller;

import com.survey_engine.payments.dto.PaymentEventDetails;
import com.survey_engine.payments.dto.PaymentEventRequest;
import com.survey_engine.payments.dto.PaymentEventResponse;
import com.survey_engine.payments.dto.TopUpRequest;
import com.survey_engine.payments.service.PaymentEventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    private final com.survey_engine.user.UserApi userApi;

    /**
     * Initializes a new payment.
     *
     * @param jwt            The authenticated user's JWT, injected by Spring Security.
     * @param paymentRequest The request body containing payment details.
     * @return A ResponseEntity containing the authorization URL for the frontend.
     */
    @PostMapping
    public Mono<ResponseEntity<PaymentEventResponse>> initializePaymentEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentEventRequest paymentRequest) {

        String userId = jwt.getSubject();
        String userEmail = resolveEmail(userId, jwt);

        log.info("Initializing payment for user {} and survey {}", userId, paymentRequest.surveyId());
        return paymentService.createPaymentEvent(paymentRequest, userId, userEmail)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Initializes a wallet top-up payment.
     *
     * @param jwt          The authenticated user's JWT.
     * @param topUpRequest The request body containing the amount and currency.
     * @return A ResponseEntity containing the authorization URL.
     */
    @PostMapping("/top-up")
    public Mono<ResponseEntity<PaymentEventResponse>> topUpWallet(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TopUpRequest topUpRequest) {

        String userId = jwt.getSubject();
        String userEmail = resolveEmail(userId, jwt);

        // Construct the internal PaymentEventRequest with the magic string
        PaymentEventRequest internalRequest = new PaymentEventRequest(
                topUpRequest.amount(),
                topUpRequest.currency(),
                "WALLET_TOPUP",
                UUID.randomUUID().toString() // Generate a unique idempotency key for this top-up
        );

        log.info("Initializing wallet top-up for user {}", userId);
        return paymentService.createPaymentEvent(internalRequest, userId, userEmail)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    private String resolveEmail(String userId, Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            java.util.Map<String, String> details = userApi.findUserDetailsMapById(userId);
            email = details.get("email");
        }
        if (email == null || email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User email is required for payment processing.");
        }
        return email;
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