package com.survey_engine.business_integration.controller;

import com.survey_engine.business_integration.dto.CreateIntegrationRequest;
import com.survey_engine.business_integration.dto.IntegrationResponse;
import com.survey_engine.business_integration.service.DarajaIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class BusinessIntegrationController {

    private final DarajaIntegrationService integrationService;

    @PostMapping
    public ResponseEntity<IntegrationResponse> createIntegration(
            @Valid @RequestBody CreateIntegrationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(integrationService.createIntegration(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<IntegrationResponse>> getIntegrations(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(integrationService.getIntegrations());
    }
}
