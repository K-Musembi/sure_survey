package com.survey_engine.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for Tenant entity
 * @param name
 * @param slug
 */
public record TenantRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(min = 3, max = 50, message = "Slug must be between 3 and 50 characters")
        String slug
) {}
