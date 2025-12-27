package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for linking a plan to a payment gateway (User module).
 */
public record PlanGatewayConfigurationRequest(
        @Sanitize
        @NotBlank(message = "Gateway type is required")
        String gatewayType, // e.g., "PAYSTACK"

        @Sanitize
        @NotBlank(message = "Gateway plan code is required")
        String gatewayPlanCode
) {}
