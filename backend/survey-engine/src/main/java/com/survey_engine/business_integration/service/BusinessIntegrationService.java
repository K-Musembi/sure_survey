package com.survey_engine.business_integration.service;

import com.survey_engine.business_integration.dto.CreateIntegrationRequest;
import com.survey_engine.business_integration.dto.DarajaConfirmationRequest;
import com.survey_engine.business_integration.dto.IntegrationResponse;
import com.survey_engine.business_integration.models.BusinessIntegration;
import com.survey_engine.business_integration.models.BusinessTransaction;
import com.survey_engine.business_integration.repository.BusinessIntegrationRepository;
import com.survey_engine.business_integration.repository.BusinessTransactionRepository;
import com.survey_engine.common.events.BusinessTransactionEvent;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessIntegrationService {

    private final BusinessIntegrationRepository integrationRepository;
    private final BusinessTransactionRepository transactionRepository;
    private final UserApi userApi;
    private final ApplicationEventPublisher eventPublisher;
    private final DarajaApiClient darajaApiClient;

    @Value("${app-security.base-url}") // e.g., https://api.sure-survey.com
    private String baseUrl;

    /**
     * Creates a new business integration configuration for the current tenant.
     * Enforces restrictions on default 'www' tenant users.
     *
     * @param request The request DTO containing integration details.
     * @return The created integration response DTO.
     * @throws IllegalStateException if the user belongs to the default tenant.
     */
    @Transactional
    public IntegrationResponse createIntegration(CreateIntegrationRequest request) {
        Long tenantId = userApi.getTenantId();
        
        // Ensure not default 'www' tenant or individual user
        String tenantName = userApi.findTenantNameById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        
        if ("Main Tenant".equalsIgnoreCase(tenantName) || "www".equalsIgnoreCase(tenantName)) { // Assuming 'www' slug maps to 'Main Tenant' or similar default
             // A better check might be based on roles or subscription plan, but for now enforcing tenant structure
             // If tenant is the default one, we might assume they are individual users.
             userApi.findTenantById(tenantId).ifPresent(t -> {
                 if ("www".equals(t.getSlug())) {
                     throw new IllegalStateException("Individual users cannot create business integrations. Please upgrade to an Enterprise account.");
                 }
             });
        }

        BusinessIntegration integration = new BusinessIntegration();
        integration.setTenantId(tenantId);
        integration.setBusinessName(request.businessName());
        integration.setType(request.type());
        integration.setSurveyId(request.surveyId());
        integration.setShortcode(request.shortcode());
        integration.setConsumerKey(request.consumerKey());
        integration.setConsumerSecret(request.consumerSecret());
        
        // Generate a random secret for the callback URL
        String secretToken = UUID.randomUUID().toString().replace("-", "");
        integration.setCallbackSecretToken(secretToken);

        BusinessIntegration saved = integrationRepository.save(integration);

        // Auto-register URLs if credentials are provided
        if (request.consumerKey() != null && !request.consumerKey().isBlank() &&
            request.consumerSecret() != null && !request.consumerSecret().isBlank()) {
            
            try {
                log.info("Auto-registering Daraja URLs for integration {}", saved.getId());
                String accessToken = darajaApiClient.getAccessToken(request.consumerKey(), request.consumerSecret());
                
                String validationUrl = String.format("%s/api/v1/integrations/webhook/daraja/%s/%s/validation", 
                        baseUrl, saved.getId(), secretToken);
                String confirmationUrl = String.format("%s/api/v1/integrations/webhook/daraja/%s/%s/confirmation", 
                        baseUrl, saved.getId(), secretToken);
                
                darajaApiClient.registerUrl(accessToken, request.shortcode(), validationUrl, confirmationUrl);
                log.info("Successfully registered URLs for integration {}", saved.getId());
                
            } catch (Exception e) {
                log.error("Failed to auto-register URLs for integration {}: {}", saved.getId(), e.getMessage());
                // We don't roll back the transaction because the integration itself is valid, 
                // but the external registration failed. The user can retry or register manually.
                // However, throwing an exception alerts the user immediately.
                throw new RuntimeException("Integration created, but failed to register URLs with Daraja: " + e.getMessage());
            }
        }

        return mapToResponse(saved);
    }

    /**
     * Retrieves all business integrations for the current tenant.
     *
     * @return A list of integration response DTOs.
     */
    @Transactional(readOnly = true)
    public List<IntegrationResponse> getIntegrations() {
        Long tenantId = userApi.getTenantId();
        return integrationRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Processes a confirmation callback from Daraja.
     * @param integrationId The ID of the integration from the URL.
     * @param secretToken The secret token from the URL to verify authenticity.
     * @param payload The Daraja payload.
     */
    @Transactional
    public void processDarajaConfirmation(UUID integrationId, String secretToken, DarajaConfirmationRequest payload) {
        BusinessIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new EntityNotFoundException("Integration not found"));

        // 1. Verify Secret
        if (!integration.getCallbackSecretToken().equals(secretToken)) {
            log.warn("Invalid secret token for integration {}", integrationId);
            throw new SecurityException("Invalid callback token");
        }

        if (!integration.isActive()) {
            log.info("Integration {} is inactive. Ignoring transaction.", integrationId);
            return;
        }

        // 2. Check Idempotency (External Transaction ID)
        if (transactionRepository.findByExternalTransactionId(payload.transId()).isPresent()) {
            log.info("Transaction {} already processed.", payload.transId());
            return;
        }

        // 3. Save Transaction
        BusinessTransaction transaction = new BusinessTransaction();
        transaction.setIntegration(integration);
        transaction.setTenantId(integration.getTenantId());
        transaction.setExternalTransactionId(payload.transId());
        transaction.setMsisdn(payload.msisdn());
        transaction.setFirstName(payload.firstName());
        transaction.setLastName(payload.lastName());
        
        if (payload.transAmount() != null) {
            transaction.setAmount(new BigDecimal(payload.transAmount()));
        }
        
        if (payload.transTime() != null) {
            try {
                // Daraja format: YYYYMMDDHHmmss
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                transaction.setTransactionTime(LocalDateTime.parse(payload.transTime(), formatter));
            } catch (Exception e) {
                log.warn("Failed to parse transaction time: {}", payload.transTime());
                transaction.setTransactionTime(LocalDateTime.now());
            }
        } else {
            transaction.setTransactionTime(LocalDateTime.now());
        }

        transactionRepository.save(transaction);

        // 4. Publish Event
        BusinessTransactionEvent event = new BusinessTransactionEvent(
                transaction.getId(),
                integration.getSurveyId(),
                transaction.getMsisdn(),
                transaction.getFirstName(),
                transaction.getLastName(),
                transaction.getAmount(),
                transaction.getTransactionTime()
        );
        eventPublisher.publishEvent(event);
        log.info("Published business transaction event for survey {}", integration.getSurveyId());
    }

    private IntegrationResponse mapToResponse(BusinessIntegration integration) {
        String callbackUrl = String.format("%s/api/v1/integrations/webhook/daraja/%s/%s/confirmation", 
                baseUrl, integration.getId(), integration.getCallbackSecretToken());
        
        return new IntegrationResponse(
                integration.getId(),
                integration.getBusinessName(),
                integration.getType(),
                integration.getShortcode(),
                callbackUrl,
                integration.isActive()
        );
    }
}
