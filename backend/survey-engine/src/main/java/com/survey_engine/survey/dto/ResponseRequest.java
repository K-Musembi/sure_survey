package com.survey_engine.survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for submitting a survey Response.
 * @param answers A list of answers for the survey questions.
 */
public record ResponseRequest(
        @NotEmpty(message = "A response must contain at least one answer.")
        @Valid
        List<AnswerRequest> answers
) {
}
