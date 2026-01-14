package com.survey_engine.survey.dto;

import java.util.Map;

/**
 * Represents the payload for a survey response submission message.
 * This record is sent to the RabbitMQ queue for asynchronous processing.
 *
 * @param surveyId The ID of the survey being responded to.
 * @param request The original request DTO containing the answers.
 * @param participantId The ID of the user submitting the response (can be null).
 * @param sessionId The session ID (e.g. phone number) if applicable.
 * @param metadata Contextual metadata (e.g., attribution info) to be saved with the response.
 */
public record ResponseSubmissionPayload(
        Long surveyId,
        ResponseRequest request,
        String participantId,
        String sessionId,
        Map<String, String> metadata
) {
}