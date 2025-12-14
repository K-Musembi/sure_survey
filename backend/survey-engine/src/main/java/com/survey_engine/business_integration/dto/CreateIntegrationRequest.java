package com.survey_engine.business_integration.dto;

import com.survey_engine.business_integration.models.enums.BusinessIntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIntegrationRequest(
        @NotBlank String businessName,
        @NotNull BusinessIntegrationType type,
        @NotNull Long surveyId,
        @NotBlank String shortcode,
        String consumerKey,
        String consumerSecret
) {}
