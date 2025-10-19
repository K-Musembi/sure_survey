package com.survey_engine.payments.controller;

import com.survey_engine.payments.dto.PaymentEventDetails;
import com.survey_engine.payments.dto.PaymentEventRequest;
import com.survey_engine.payments.dto.PaymentEventResponse;
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
        String userEmail = jwt.getClaimAsString("email");

        log.info("Initializing payment for user {} and survey {}", userId, paymentRequest.surveyId());
        return paymentService.createPaymentEvent(paymentRequest, userId, userEmail)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
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