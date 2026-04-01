package com.survey_engine.survey.controller;

import com.survey_engine.survey.models.WebhookDeliveryLog;
import com.survey_engine.survey.models.WebhookSubscription;
import com.survey_engine.survey.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages outbound webhook subscriptions for tenants.
 * Tenants can subscribe to platform events and receive HTTP POST callbacks.
 */
@RestController
@RequestMapping("/api/v1/webhooks/subscriptions")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookDeliveryService webhookDeliveryService;

    @PostMapping
    public ResponseEntity<WebhookSubscription> createSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {
        Long tenantId = jwt.getClaim("tenantId");
        WebhookSubscription sub = webhookDeliveryService.createSubscription(
                tenantId,
                request.get("targetUrl"),
                request.get("eventTypes"),
                request.get("secret")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(sub);
    }

    @GetMapping
    public ResponseEntity<List<WebhookSubscription>> getSubscriptions(
            @AuthenticationPrincipal Jwt jwt) {
        Long tenantId = jwt.getClaim("tenantId");
        return ResponseEntity.ok(webhookDeliveryService.getSubscriptionsForTenant(tenantId));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestParam boolean active) {
        Long tenantId = jwt.getClaim("tenantId");
        webhookDeliveryService.toggleSubscription(tenantId, id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        Long tenantId = jwt.getClaim("tenantId");
        webhookDeliveryService.deleteSubscription(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/deliveries")
    public ResponseEntity<List<WebhookDeliveryLog>> getDeliveryLogs(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        Long tenantId = jwt.getClaim("tenantId");
        return ResponseEntity.ok(webhookDeliveryService.getDeliveryLogs(tenantId, id));
    }
}
