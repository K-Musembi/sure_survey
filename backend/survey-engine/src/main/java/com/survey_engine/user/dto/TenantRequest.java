package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for Tenant entity.
 *
 * @param name The human-readable name of the tenant.
 * @param slug A unique, URL-friendly identifier for the tenant (e.g., subdomain).
 */
public record TenantRequest(

        @Sanitize
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        // @NotBlank(message = "Slug is required")
        @Sanitize
        @Size(max = 50, message = "Slug must have max 50 characters")
        String slug
) {}
