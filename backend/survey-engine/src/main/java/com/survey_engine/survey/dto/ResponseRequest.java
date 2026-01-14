package com.survey_engine.survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for submitting a survey Response.
 * @param answers A list of answers for the survey questions.
 * @param metadata Optional metadata to be stored with the response (e.g., attribution).
 */
public record ResponseRequest(
        @NotEmpty(message = "A response must contain at least one answer.")
        @Valid
        List<AnswerRequest> answers,

        Map<String, String> metadata
) {
}