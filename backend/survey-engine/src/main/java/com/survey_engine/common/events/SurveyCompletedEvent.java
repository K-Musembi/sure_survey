package com.survey_engine.common.events;

/**
 * A Data Transfer Object (DTO) representing the payload for a completed survey response.
 * This event is published via Spring Application Events when a survey response is successfully processed.
 * It decouples the consuming module (e.g., 'rewards') from the 'survey' module.
 *
 * @param surveyId The ID of the survey that was completed.
 * @param responseId The ID of the response that was submitted.
 * @param responderId The canonical ID of the user or participant who responded.
 */
public record SurveyCompletedEvent(
        Long surveyId,
        Long responseId,
        String responderId
) {}