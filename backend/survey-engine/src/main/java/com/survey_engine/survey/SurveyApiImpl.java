package com.survey_engine.survey;

import com.survey_engine.survey.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link SurveyApi} interface.
 * Provides concrete access to survey-related repositories for inter-module communication.
 */
@Service
@RequiredArgsConstructor
class SurveyApiImpl implements SurveyApi {

    private final ResponseRepository responseRepository;

    /**
     * Retrieves the {@link ResponseRepository} instance.
     *
     * @return The {@link ResponseRepository} instance.
     */
    @Override
    public ResponseRepository getResponseRepository() {
        return responseRepository;
    }
}