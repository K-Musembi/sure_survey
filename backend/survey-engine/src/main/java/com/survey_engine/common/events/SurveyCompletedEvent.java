package com.survey_engine.common.events;

/**
 * An event published when a survey response has been successfully submitted and processed.
 *
 * @param surveyId The ID of the survey that was completed.
 * @param responseId The ID of the response that was created.
 * @param responderId The ID of the user or participant who submitted the response. Can be null for anonymous responses.
 */
public record SurveyCompletedEvent(
        Long surveyId,
        Long responseId,
        String responderId
) {
}
