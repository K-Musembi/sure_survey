package com.survey_engine.survey.answer.dto;

import com.survey_engine.survey.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating an Answer.
 * @param questionId The ID of the question being answered.
 * @param answerValue The value of the answer provided by the user.
 */
public record AnswerRequest(
        @NotNull(message = "Question ID cannot be null")
        Long questionId,

        @Sanitize
        @NotBlank(message = "Answer value cannot be blank")
        String answerValue
) {
}
