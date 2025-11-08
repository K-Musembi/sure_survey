package com.survey_engine.billing.controller;

import com.survey_engine.billing.dto.InvoiceResponse;
import com.survey_engine.billing.dto.PlanResponse;
import com.survey_engine.billing.dto.SubscriptionRequest;
import com.survey_engine.billing.dto.SubscriptionResponse;
import com.survey_engine.billing.models.Invoice;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.service.InvoiceService;
import com.survey_engine.billing.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing billing-related operations, including subscriptions and invoices.
 * This controller provides endpoints for users to view their subscription status, manage plans,
 * and access their invoices.
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BillingController {

    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;

    /**
     * Retrieves the current active subscription for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @return A {@link ResponseEntity} containing the {@link SubscriptionResponse} or 404 if not found.
     */
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> getSubscription(@AuthenticationPrincipal Jwt jwt) {
        Long tenantId = jwt.getClaim("tenantId");
        Long userId = Long.valueOf(jwt.getSubject());
        Optional<Subscription> subscription = subscriptionService.getActiveSubscriptionForUser(tenantId, userId);
        return subscription.map(this::mapToSubscriptionResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new subscription for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @param request The {@link SubscriptionRequest} containing the plan ID.
     * @return A {@link ResponseEntity} containing the newly created {@link SubscriptionResponse}.
     */
    @PostMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubscriptionRequest request) {
        Long tenantId = jwt.getClaim("tenantId");
        Long userId = Long.valueOf(jwt.getSubject());
        Subscription newSubscription = subscriptionService.createSubscription(tenantId, userId, request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSubscriptionResponse(newSubscription));
    }

    /**
     * Cancels the active subscription for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @param subscriptionId The ID of the subscription to cancel.
     * @return A {@link ResponseEntity} with no content.
     */
    @DeleteMapping("/subscription/{subscriptionId}")
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID subscriptionId) {
        Long tenantId = jwt.getClaim("tenantId");
        Long userId = Long.valueOf(jwt.getSubject());
        subscriptionService.cancelSubscription(subscriptionId, tenantId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all invoices for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @return A {@link ResponseEntity} containing a list of {@link InvoiceResponse}.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(@AuthenticationPrincipal Jwt jwt) {
        Long tenantId = jwt.getClaim("tenantId");
        Long userId = Long.valueOf(jwt.getSubject());
        List<Invoice> invoices = invoiceService.findInvoicesForUser(tenantId, userId);
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceResponses);
    }

    /**
     * Maps a {@link Subscription} entity to a {@link SubscriptionResponse} DTO.
     *
     * @param subscription The {@link Subscription} entity to map.
     * @return The corresponding {@link SubscriptionResponse} DTO.
     */
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        PlanResponse planResponse = new PlanResponse(
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getPlan().getPrice(),
                subscription.getPlan().getBillingInterval(),
                subscription.getPlan().getFeatures()
        );
        return new SubscriptionResponse(
                subscription.getId(),
                planResponse,
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getTrialEndDate()
        );
    }

    /**
     * Maps an {@link Invoice} entity to an {@link InvoiceResponse} DTO.
     *
     * @param invoice The {@link Invoice} entity to map.
     * @return The corresponding {@link InvoiceResponse} DTO.
     */
    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getSubscription() != null ? invoice.getSubscription().getId() : null,
                invoice.getStatus(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getPaidAt(),
                invoice.getInvoicePdfUrl()
        );
    }
}