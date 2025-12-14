package com.survey_engine.business_integration.dto;

import com.survey_engine.business_integration.models.enums.BusinessIntegrationType;

import java.util.UUID;

public record IntegrationResponse(
        UUID id,
        String businessName,
        BusinessIntegrationType type,
        String shortcode,
        String callbackUrl, // The URL they need to copy to Daraja
        boolean isActive
) {}
