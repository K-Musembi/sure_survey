package com.survey_engine.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for Company entity
 * @param name
 * @param sector
 * @param country
 */
public record CompanyRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Sector is required")
        @Size(min = 3, max = 50, message = "Sector must be between 3 and 50 characters")
        String sector,

        @NotBlank(message = "Country is required")
        @Size(min = 3, max = 50, message = "Country must be between 3 and 50 characters")
        String country
) {}
