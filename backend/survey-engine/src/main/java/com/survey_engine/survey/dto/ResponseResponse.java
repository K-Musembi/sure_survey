package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a survey Response.
 * @param id The unique identifier of the response.
 * @param surveyId The ID of the survey that was responded to.
 * @param status The status of the response (e.g., COMPLETED).
 * @param submissionDate The date and time the response was submitted.
 * @param userId The ID of the user who submitted the response.
 * @param answers The list of answers included in the response.
 */
public record ResponseResponse(
        Long id,
        Long surveyId,
        ResponseStatus status,
        LocalDateTime submissionDate,
        String userId,
        List<AnswerResponse> answers
) {
}
