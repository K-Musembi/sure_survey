package com.survey_engine.survey.question.dto;

import com.survey_engine.survey.common.enums.QuestionType;
import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request Data Transfer Object (DTO) for Question entity
 * @param questionText
 * @param questionType
 * @param options
 * @param position
 */
public record QuestionRequest(
        @NotBlank(message = "Question text is required")
        @Sanitize
        String questionText,

        @NotNull(message = "Question type is required")
        QuestionType questionType,

        @Sanitize
        String options, // JSON string for options

        @NotNull(message = "Question position is required")
        Integer position
) {
}