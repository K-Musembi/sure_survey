package com.survey_engine.survey;

import com.survey_engine.survey.repository.ResponseRepository;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Map;

/**
 * Public API for the Survey module.
 * This interface allows other modules to interact with survey-related data
 * without direct dependencies on the internal implementation details of the 'survey' module.
 */
@NamedInterface
public interface SurveyApi {

    /**
     * Retrieves the {@link ResponseRepository} for direct access to survey response data.
     * This is primarily intended for internal module communication where direct repository access is necessary.
     *
     * @return The {@link ResponseRepository} instance.
     */
    ResponseRepository getResponseRepository();

    /**
     * Retrieves a list of survey data for a given tenant ID.
     * Each survey is represented as a Map containing a subset of its fields
     * to avoid leaking the full entity across module boundaries.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Maps, where each Map represents a survey.
     */
    List<Map<String, Object>> findSurveysByTenantId(Long tenantId);
}
