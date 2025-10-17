package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ParticipantRequest(
        @Sanitize
        @NotBlank(message = "Full name is required")
        String fullName,

        @Sanitize
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @Sanitize
        @Email(message = "A valid email is required")
        String email
) {}