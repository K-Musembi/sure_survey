package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.AccessType;
import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request Data Transfer Object (DTO) for Survey entity
 * @param name
 * @param type
 * @param accessType
 * @param startDate
 * @param endDate
 * @param questions
 */
public record SurveyRequest(
        @NotBlank(message = "Survey name is required")
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        @Sanitize
        String name,

        @Sanitize
        String introduction,

        @NotNull(message = "Survey type is required")
        SurveyType type,

        @NotNull(message = "Access type is required")
        AccessType accessType,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Integer targetRespondents,

        java.math.BigDecimal budget,

        @Valid
        @NotNull(message = "Questions list cannot be null")
        @Size(min = 1, message = "Survey must have at least one question")
        List<QuestionRequest> questions
) {
}
