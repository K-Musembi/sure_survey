package com.survey_engine.survey;

import com.survey_engine.survey.repository.ResponseRepository;
import org.springframework.modulith.NamedInterface;

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
}