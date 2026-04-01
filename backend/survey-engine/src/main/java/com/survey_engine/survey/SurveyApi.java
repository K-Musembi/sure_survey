package com.survey_engine.survey;

import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public API for the Survey module.
 * This interface allows other modules to interact with survey-related data
 * without direct dependencies on the internal implementation details of the 'survey' module.
 */
@NamedInterface
public interface SurveyApi {

    /**
     * Counts the number of responses for a given survey.
     *
     * @param surveyId The ID of the survey.
     * @return The number of responses.
     */
    long countResponsesBySurveyId(Long surveyId);

    /**
     * Retrieves a single response's answer data by response ID.
     * Returns a map with keys: id, surveyId, participantId, sessionId, submissionDate,
     * metadata (JSON string), answers (list of maps with questionId, answerValue).
     *
     * @param responseId The ID of the response.
     * @return An Optional containing the response data map, or empty if not found.
     */
    Optional<Map<String, Object>> getResponseById(Long responseId);

    /**
     * Retrieves a list of survey data for a given tenant ID.
     * Each survey is represented as a Map containing a subset of its fields
     * to avoid leaking the full entity across module boundaries.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Maps, where each Map represents a survey.
     */
    List<Map<String, Object>> findSurveysByTenantId(Long tenantId);

    /**
     * Retrieves a list of survey data for a given user ID.
     *
     * @param userId The ID of the user.
     * @return A list of Maps, where each Map represents a survey.
     */
    List<Map<String, Object>> findSurveysByUserId(String userId);

    /**
     * Retrieves all text responses for a specific survey ID, formatted for consumption.
     *
     * @param surveyId The ID of the survey.
     * @return A list of formatted response strings.
     */
    List<String> getSurveyResponseTexts(Long surveyId);

    /**
     * Retrieves a single survey with its questions and summarised response data.
     * Used by the intelligence module for report generation.
     * Returns null if the survey does not exist.
     *
     * @param surveyId The ID of the survey.
     * @return A Map representing the survey and its data, or null.
     */
    Map<String, Object> getSurveyById(Long surveyId);

    /**
     * Counts surveys owned by a specific user (efficient COUNT query, no entity loading).
     */
    long countSurveysByUserId(String userId);

    /**
     * Counts surveys belonging to a specific tenant (efficient COUNT query, no entity loading).
     */
    long countSurveysByTenantId(Long tenantId);

    /**
     * Returns the total number of surveys across the platform.
     */
    long getPlatformSurveyCount();

    /**
     * Returns the total number of responses across the platform.
     */
    long getPlatformResponseCount();
}
