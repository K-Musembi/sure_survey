package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for checking tenant name similarity.
 *
 * @param tenantName The name of the tenant to check for similarity.
 */
public record CheckTenantRequest(
        @Sanitize
        @NotBlank(message = "Tenant name is required")
        @Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
        String tenantName
) {}