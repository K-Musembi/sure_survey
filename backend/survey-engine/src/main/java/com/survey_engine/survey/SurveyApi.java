package com.survey_engine.survey;

import com.survey_engine.survey.dto.ResponseResponse;
import com.survey_engine.survey.repository.ResponseRepository;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface SurveyApi {

    ResponseRepository getResponseRepository();
}
