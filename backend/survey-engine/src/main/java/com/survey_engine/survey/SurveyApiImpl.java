package com.survey_engine.survey;

import com.survey_engine.survey.models.Response;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.ResponseRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SurveyApi} interface.
 * Provides concrete access to survey-related repositories for inter-module communication.
 */
@Service
@RequiredArgsConstructor
class SurveyApiImpl implements SurveyApi {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;

    @org.springframework.beans.factory.annotation.Value("${survey.web.base-url}")
    private String webBaseUrl;

    /**
     * Retrieves the {@link ResponseRepository} instance.
     *
     * @return The {@link ResponseRepository} instance.
     */
    @Override
    public ResponseRepository getResponseRepository() {
        return responseRepository;
    }

    /**
     * Retrieves all surveys for a given tenant ID and maps them to a list of Maps.
     * This prevents leaking the Survey entity to other modules.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Maps, where each map contains key survey attributes.
     */
    @Override
    public List<Map<String, Object>> findSurveysByTenantId(Long tenantId) {
        List<Survey> surveys = surveyRepository.findByTenantId(tenantId);
        return mapSurveysToMaps(surveys);
    }

    @Override
    public List<Map<String, Object>> findSurveysByUserId(String userId) {
        List<Survey> surveys = surveyRepository.findByUserId(userId);
        return mapSurveysToMaps(surveys);
    }

    private List<Map<String, Object>> mapSurveysToMaps(List<Survey> surveys) {
        return surveys.stream()
                .map(survey -> {
                    Map<String, Object> surveyMap = new HashMap<>();
                    surveyMap.put("id", survey.getId());
                    surveyMap.put("name", survey.getName());
                    surveyMap.put("introduction", survey.getIntroduction());
                    surveyMap.put("type", survey.getType().toString());
                    surveyMap.put("userId", survey.getUserId());
                    surveyMap.put("status", survey.getStatus().toString());
                    surveyMap.put("accessType", survey.getAccessType().toString());
                    surveyMap.put("startDate", survey.getStartDate());
                    surveyMap.put("endDate", survey.getEndDate());
                    surveyMap.put("createdAt", survey.getCreatedAt());
                    surveyMap.put("url_code", survey.getUrlCode());
                    surveyMap.put("web_url", webBaseUrl + survey.getUrlCode());
                    return surveyMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSurveyResponseTexts(Long surveyId) {
        List<Response> responses = responseRepository.findBySurveyId(surveyId);
        return responses.stream()
                .flatMap(r -> r.getAnswers().stream())
                .map(a -> "Q: " + a.getQuestion().getQuestionText() + " | A: " + a.getAnswerValue())
                .collect(Collectors.toList());
    }
}
