package com.survey_engine.survey.dto;

import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DistributionListRequest(
        @NotBlank(message = "Name is required")
        @Sanitize
        String name,

        @NotNull(message = "Contacts list cannot be null")
        List<ContactRequest> contacts
) {
}