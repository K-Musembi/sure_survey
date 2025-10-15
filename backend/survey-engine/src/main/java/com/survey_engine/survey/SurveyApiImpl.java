package com.survey_engine.survey;

import com.survey_engine.survey.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class SurveyApiImpl implements SurveyApi {

    private final ResponseRepository responseRepository;

    @Override
    public ResponseRepository getResponseRepository() {
        return responseRepository;
    }
}
