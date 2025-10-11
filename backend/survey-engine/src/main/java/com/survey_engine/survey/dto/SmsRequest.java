package com.survey_engine.survey.dto;

import jakarta.validation.constraints.NotBlank;

public record SmsRequest(
        @NotBlank(message = "Recipient phone number ('to') is required")
        String to,

        @NotBlank(message = "SMS 'message' content is required")
        String message
) {
}
