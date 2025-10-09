package com.survey_engine.survey.template_question.dto;

import com.survey_engine.survey.common.enums.QuestionType;
import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a TemplateQuestion.
 * @param questionText The text of the question.
 * @param questionType The type of the question (e.g., FREE_TEXT).
 * @param options The options for multiple-choice questions, as a JSON string.
 * @param position The display order of the question.
 */
public record TemplateQuestionRequest(
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
