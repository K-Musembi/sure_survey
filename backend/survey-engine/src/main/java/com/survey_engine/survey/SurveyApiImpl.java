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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SurveyApi} interface.
 * Provides concrete access to survey-related data for inter-module communication.
 */
@Service
@RequiredArgsConstructor
class SurveyApiImpl implements SurveyApi {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;

    @org.springframework.beans.factory.annotation.Value("${survey.web.base-url}")
    private String webBaseUrl;

    @Override
    public long countResponsesBySurveyId(Long surveyId) {
        return responseRepository.countBySurveyId(surveyId);
    }

    @Override
    public Optional<Map<String, Object>> getResponseById(Long responseId) {
        return responseRepository.findById(responseId)
                .map(response -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", response.getId());
                    map.put("surveyId", response.getSurvey().getId());
                    map.put("surveyUserId", response.getSurvey().getUserId());
                    map.put("participantId", response.getParticipantId());
                    map.put("sessionId", response.getSessionId());
                    map.put("submissionDate", response.getSubmissionDate());
                    map.put("metadata", response.getMetadata());
                    map.put("answers", response.getAnswers().stream()
                            .map(a -> Map.of(
                                    "questionId", (Object) a.getQuestion().getId(),
                                    "answerValue", (Object) a.getAnswerValue()
                            )).collect(Collectors.toList()));
                    return map;
                });
    }

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

    @Override
    public long countSurveysByUserId(String userId) {
        return surveyRepository.countByUserId(userId);
    }

    @Override
    public long countSurveysByTenantId(Long tenantId) {
        return surveyRepository.countByTenantId(tenantId);
    }

    @Override
    public long getPlatformSurveyCount() {
        return surveyRepository.count();
    }

    @Override
    public long getPlatformResponseCount() {
        return responseRepository.count();
    }

    @Override
    public Map<String, Object> getSurveyById(Long surveyId) {
        return surveyRepository.findById(surveyId)
                .map(survey -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", survey.getId());
                    map.put("name", survey.getName());
                    map.put("type", survey.getType().toString());
                    map.put("status", survey.getStatus().toString());
                    map.put("userId", survey.getUserId());
                    map.put("urlCode", survey.getUrlCode());

                    // Include questions with scoring metadata
                    List<Map<String, Object>> questionMaps = survey.getQuestions().stream()
                            .map(q -> {
                                Map<String, Object> qm = new HashMap<>();
                                qm.put("id", q.getId());
                                qm.put("questionText", q.getQuestionText());
                                qm.put("questionType", q.getQuestionType().toString());
                                qm.put("options", q.getOptions());
                                qm.put("category", q.getCategory());
                                qm.put("weight", q.getWeight());
                                return qm;
                            }).collect(Collectors.toList());
                    map.put("questions", questionMaps);

                    // Include response summaries
                    List<Response> responses = responseRepository.findBySurveyId(surveyId);
                    map.put("responses", responses.stream()
                            .map(r -> {
                                Map<String, Object> rm = new HashMap<>();
                                rm.put("id", r.getId());
                                rm.put("participantId", r.getParticipantId());
                                rm.put("submissionDate", r.getSubmissionDate());
                                rm.put("answers", r.getAnswers().stream()
                                        .map(a -> Map.of(
                                                "questionId", a.getQuestion().getId(),
                                                "answerValue", a.getAnswerValue()
                                        )).collect(Collectors.toList()));
                                return rm;
                            }).collect(Collectors.toList()));
                    return map;
                })
                .orElse(null);
    }
}
