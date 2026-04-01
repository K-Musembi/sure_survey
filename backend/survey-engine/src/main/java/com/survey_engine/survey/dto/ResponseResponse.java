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
 * @param participantId The ID of the user who submitted the response.
 * @param answers The list of answers included in the response.
 * @param nextQuestionId The ID of the next question (from branch rules). Null = end survey, absent = linear progression.
 */
public record ResponseResponse(
        Long id,
        Long surveyId,
        ResponseStatus status,
        LocalDateTime submissionDate,
        String participantId,
        List<AnswerResponse> answers,
        Long nextQuestionId
) {
    /** Constructor without nextQuestionId for backwards compatibility (listing/retrieval). */
    public ResponseResponse(Long id, Long surveyId, ResponseStatus status,
                            LocalDateTime submissionDate, String participantId,
                            List<AnswerResponse> answers) {
        this(id, surveyId, status, submissionDate, participantId, answers, null);
    }
}
