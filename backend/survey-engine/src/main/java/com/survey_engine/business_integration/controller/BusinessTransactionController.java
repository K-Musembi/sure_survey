package com.survey_engine.business_integration.controller;

import com.survey_engine.business_integration.dto.BusinessTransactionResponse;
import com.survey_engine.business_integration.service.BusinessTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-transactions")
@RequiredArgsConstructor
public class BusinessTransactionController {

    private final BusinessTransactionService transactionService;

    /**
     * Retrieves all transactions for a specific integration.
     * The service layer handles tenant-based security using the authenticated context.
     *
     * @param integrationId The ID of the integration.
     * @param jwt The authenticated user's JWT (used for context).
     * @return A list of transactions.
     */
    @GetMapping("/integration/{integrationId}")
    public ResponseEntity<List<BusinessTransactionResponse>> getTransactionsByIntegration(
            @PathVariable UUID integrationId,
            @AuthenticationPrincipal Jwt jwt) {
        // The service uses UserApi to get tenantId from context, which is set by the security filter using the JWT/Headers.
        // Explicitly passing/checking userId here isn't strictly necessary for tenant isolation if the service relies on TenantContext,
        // but ensuring the context is active is key.
        return ResponseEntity.ok(transactionService.getTransactionsByIntegrationId(integrationId));
    }
}
