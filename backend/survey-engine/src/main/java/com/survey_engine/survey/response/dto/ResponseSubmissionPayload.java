package com.survey_engine.survey.response.dto;

/**
 * Represents the payload for a survey response submission message.
 * This record is sent to the RabbitMQ queue for asynchronous processing.
 *
 * @param surveyId The ID of the survey being responded to.
 * @param request The original request DTO containing the answers.
 * @param userId The ID of the user submitting the response (can be null).
 */
public record ResponseSubmissionPayload(
        Long surveyId,
        ResponseRequest request,
        String userId
) {
}
