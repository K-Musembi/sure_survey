package com.survey_engine.business_integration.controller;

import com.survey_engine.business_integration.dto.DarajaConfirmationRequest;
import com.survey_engine.business_integration.service.BusinessIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integrations/webhook/daraja")
@RequiredArgsConstructor
@Slf4j
public class DarajaWebhookController {

    private final BusinessIntegrationService integrationService;

    /**
     * M-Pesa Validation URL.
     * We always accept the payment.
     */
    @PostMapping("/{integrationId}/{secret}/validation")
    public ResponseEntity<Map<String, String>> validate(
            @PathVariable UUID integrationId,
            @PathVariable String secret,
            @RequestBody Map<String, Object> payload) {
        log.info("Received validation request for integration {}", integrationId);
        // Always accept
        return ResponseEntity.ok(Map.of(
                "ResultCode", "0",
                "ResultDesc", "Accepted"
        ));
    }

    /**
     * M-Pesa Confirmation URL.
     */
    @PostMapping("/{integrationId}/{secret}/confirmation")
    public ResponseEntity<Map<String, String>> confirm(
            @PathVariable UUID integrationId,
            @PathVariable String secret,
            @RequestBody DarajaConfirmationRequest payload) {
        
        log.info("Received confirmation for integration {}", integrationId);
        try {
            integrationService.processDarajaConfirmation(integrationId, secret, payload);
        } catch (Exception e) {
            log.error("Error processing confirmation for integration {}", integrationId, e);
            // We return OK to Daraja so they don't retry endlessly if it's a logic error on our side
        }
        
        return ResponseEntity.ok(Map.of(
                "ResultCode", "0",
                "ResultDesc", "Received"
        ));
    }
}
