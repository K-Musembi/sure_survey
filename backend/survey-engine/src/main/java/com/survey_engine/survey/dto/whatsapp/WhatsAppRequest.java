package com.survey_engine.survey.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;

public record WhatsAppRequest(
        @NotBlank String to,
        @NotBlank String message
) {}
