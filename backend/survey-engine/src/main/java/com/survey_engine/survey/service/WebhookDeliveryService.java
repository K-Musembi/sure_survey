package com.survey_engine.survey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.survey.models.WebhookDeliveryLog;
import com.survey_engine.survey.models.WebhookSubscription;
import com.survey_engine.survey.repository.WebhookDeliveryLogRepository;
import com.survey_engine.survey.repository.WebhookSubscriptionRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.survey_engine.common.exception.BusinessRuleException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages outbound webhook subscriptions and event delivery.
 * When platform events occur (e.g. SURVEY_COMPLETED), matching subscriptions
 * receive an HTTP POST with the event payload, signed with HMAC-SHA256.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final SurveyRepository surveyRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public WebhookSubscription createSubscription(Long tenantId, String targetUrl,
                                                   String eventTypesJson, String secret) {
        validateTargetUrl(targetUrl);
        WebhookSubscription sub = new WebhookSubscription();
        sub.setTenantId(tenantId);
        sub.setTargetUrl(targetUrl);
        sub.setEventTypes(eventTypesJson);
        sub.setSecret(secret);
        sub.setActive(true);
        return subscriptionRepository.save(sub);
    }

    private void validateTargetUrl(String targetUrl) {
        try {
            URI uri = URI.create(targetUrl);
            String scheme = uri.getScheme();
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
                throw new BusinessRuleException("INVALID_WEBHOOK_URL", "Webhook URL must use http or https");
            }
            String host = uri.getHost();
            if (host == null) {
                throw new BusinessRuleException("INVALID_WEBHOOK_URL", "Webhook URL must have a valid host");
            }
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                throw new BusinessRuleException("INVALID_WEBHOOK_URL",
                        "Webhook URL must not point to internal or private addresses");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("INVALID_WEBHOOK_URL", "Invalid webhook URL: " + e.getMessage());
        }
    }

    public List<WebhookSubscription> getSubscriptionsForTenant(Long tenantId) {
        return subscriptionRepository.findByTenantId(tenantId);
    }

    @Transactional
    public void toggleSubscription(Long tenantId, UUID subscriptionId, boolean active) {
        WebhookSubscription sub = findOwnedSubscription(tenantId, subscriptionId);
        sub.setActive(active);
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void deleteSubscription(Long tenantId, UUID subscriptionId) {
        findOwnedSubscription(tenantId, subscriptionId);
        subscriptionRepository.deleteById(subscriptionId);
    }

    public List<WebhookDeliveryLog> getDeliveryLogs(Long tenantId, UUID subscriptionId) {
        findOwnedSubscription(tenantId, subscriptionId);
        return deliveryLogRepository.findBySubscriptionIdOrderByDeliveredAtDesc(subscriptionId);
    }

    private WebhookSubscription findOwnedSubscription(Long tenantId, UUID subscriptionId) {
        WebhookSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("WEBHOOK_NOT_FOUND",
                        "Webhook subscription not found: " + subscriptionId));
        if (!sub.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("WEBHOOK_NOT_FOUND",
                    "Webhook subscription not found: " + subscriptionId);
        }
        return sub;
    }

    @Async
    @EventListener
    public void onSurveyCompleted(SurveyCompletedEvent event) {
        try {
            Long tenantId = surveyRepository.findById(event.surveyId())
                    .map(s -> s.getTenantId())
                    .orElse(null);
            if (tenantId == null) return;

            Map<String, Object> payload = Map.of(
                    "event", "SURVEY_COMPLETED",
                    "surveyId", event.surveyId(),
                    "responseId", event.responseId(),
                    "responderId", event.responderId() != null ? event.responderId() : ""
            );

            deliverToSubscribers(tenantId, "SURVEY_COMPLETED", payload);
        } catch (Exception e) {
            log.error("Webhook delivery failed for SURVEY_COMPLETED event: {}", e.getMessage(), e);
        }
    }

    private void deliverToSubscribers(Long tenantId, String eventType, Map<String, Object> payload) {
        List<WebhookSubscription> subs = subscriptionRepository.findByTenantIdAndActiveTrue(tenantId);

        for (WebhookSubscription sub : subs) {
            if (!sub.getEventTypes().contains(eventType)) continue;

            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                String signature = sub.getSecret() != null
                        ? computeHmac(payloadJson, sub.getSecret()) : null;

                WebClient client = webClientBuilder.build();
                var requestSpec = client.post()
                        .uri(sub.getTargetUrl())
                        .contentType(MediaType.APPLICATION_JSON);

                if (signature != null) {
                    requestSpec = requestSpec.header("X-Webhook-Signature", signature);
                }

                var responseSpec = requestSpec
                        .bodyValue(payloadJson)
                        .retrieve()
                        .toEntity(String.class)
                        .block(java.time.Duration.ofSeconds(10));

                int status = responseSpec != null ? responseSpec.getStatusCode().value() : 0;
                String body = responseSpec != null ? responseSpec.getBody() : null;
                boolean success = status >= 200 && status < 300;

                logDelivery(sub.getId(), eventType, payloadJson, status, body, success);

            } catch (Exception e) {
                log.error("Webhook delivery to {} failed: {}", sub.getTargetUrl(), e.getMessage());
                try {
                    String payloadJson = objectMapper.writeValueAsString(payload);
                    logDelivery(sub.getId(), eventType, payloadJson, 0, e.getMessage(), false);
                } catch (Exception ignored) {}
            }
        }
    }

    private void logDelivery(UUID subscriptionId, String eventType, String payload,
                             int httpStatus, String responseBody, boolean success) {
        WebhookDeliveryLog logEntry = new WebhookDeliveryLog();
        logEntry.setSubscriptionId(subscriptionId);
        logEntry.setEventType(eventType);
        logEntry.setPayload(payload);
        logEntry.setHttpStatus(httpStatus);
        logEntry.setResponseBody(responseBody != null && responseBody.length() > 2000
                ? responseBody.substring(0, 2000) : responseBody);
        logEntry.setSuccess(success);
        deliveryLogRepository.save(logEntry);
    }

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            log.error("HMAC computation failed", e);
            return null;
        }
    }
}
