package com.survey_engine.survey.dto;

import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
        @NotBlank(message = "Phone number is required")
        @Sanitize
        String phoneNumber,

        @Sanitize
        String firstName,

        @Sanitize
        String lastName,

        @Sanitize
        String email
) {
}
