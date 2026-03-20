package com.survey_engine.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for filing a Subject Access Request or Erasure Request.
 * The raw phone number is hashed server-side and never persisted.
 */
public record DataSubjectRequestBody(
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
        String phone
) {}
