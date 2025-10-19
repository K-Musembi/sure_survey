package com.survey_engine.survey.service;

import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.dto.QuestionRequest;
import com.survey_engine.survey.dto.QuestionResponse;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.survey.dto.SurveyRequest;
import com.survey_engine.survey.dto.SurveyResponse;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private static final Logger logger = LoggerFactory.getLogger(SurveyService.class);

    private final SurveyRepository surveyRepository;
    private final UserApi userApi;

    @Transactional
    public SurveyResponse createSurvey(SurveyRequest surveyRequest, String userId) {
        Long tenantId = userApi.getTenantId();
        if (surveyRepository.findByNameAndTenantId(surveyRequest.name(), tenantId).isPresent()) {
            throw new DataIntegrityViolationException("A survey with this name already exists");
        }

        Survey survey = new Survey();
        survey.setTenantId(tenantId);
        survey.setUserId(userId);
        Survey savedSurvey = getSurvey(survey, surveyRequest);
        return mapToSurveyResponse(savedSurvey);
    }

    @Transactional(readOnly = true)
    public SurveyResponse findSurveyById(Long id) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(id)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));
        return mapToSurveyResponse(survey);
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> findSurveysByUserId(String userId) {
        Long tenantId = userApi.getTenantId();
        List<Survey> surveys = surveyRepository.findByUserIdAndTenantId(userId, tenantId);
        return surveys.stream()
                .map(this::mapToSurveyResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> findAllSurveys(List<String> roles) {
        //Long tenantId = userApi.getTenantId();
        List<Survey> surveys;

        if (roles != null && roles.contains("SYSTEM ADMIN"))  {
            throw new AccessDeniedException("You are not allowed to view all surveys");
        }

        surveys = surveyRepository.findAll(); // Admins can see all surveys
        return surveys.stream()
                    .map(this::mapToSurveyResponse)
                    .collect(Collectors.toList());
    }

    @Transactional
    public SurveyResponse updateSurvey(Long id, SurveyRequest surveyRequest, String userId, List<String> roles) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(id)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));

        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this survey.");
        }

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Survey can only be updated when in DRAFT status.");
        }
        
        Survey savedSurvey = getSurvey(survey, surveyRequest);
        return mapToSurveyResponse(savedSurvey);
    }

    @Transactional
    public SurveyResponse activateSurvey(Long surveyId, String userId, List<String> roles) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(surveyId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to activate this survey.");
        }

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Only surveys in DRAFT status can be activated.");
        }

        if (survey.getQuestions() == null || survey.getQuestions().isEmpty()) {
            throw new IllegalStateException("Survey must have at least one question to be activated.");
        }

        survey.setStatus(SurveyStatus.ACTIVE);
        Survey savedSurvey = surveyRepository.save(survey);
        return mapToSurveyResponse(savedSurvey);
    }

    @Transactional
    public SurveyResponse closeSurvey(Long surveyId, String userId, List<String> roles) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(surveyId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to close this survey.");
        }

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Only surveys in ACTIVE status can be closed.");
        }

        survey.setStatus(SurveyStatus.CLOSED);
        Survey savedSurvey = surveyRepository.save(survey);
        return mapToSurveyResponse(savedSurvey);
    }

    @Transactional
    public void activatePaidSurvey(Long surveyId) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(surveyId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            logger.warn("Attempted to activate survey {} which is not in DRAFT status. Current status: {}.", surveyId, survey.getStatus());
            return;
        }

        if (survey.getQuestions() == null || survey.getQuestions().isEmpty()) {
            logger.error("Attempted to activate survey {} with no questions.", surveyId);
            return;
        }

        survey.setStatus(SurveyStatus.ACTIVE);
        surveyRepository.save(survey);
        logger.info("Successfully activated survey {} after payment.", surveyId);
    }

    @Transactional
    public void deleteSurvey(Long id, String userId, List<String> roles) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(id)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));
        
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this survey.");
        }

        surveyRepository.delete(survey);
    }

    private Survey getSurvey(Survey survey, SurveyRequest surveyRequest) {
        survey.setName(surveyRequest.name());
        survey.setType(surveyRequest.type());
        survey.setAccessType(surveyRequest.accessType());
        survey.setStartDate(surveyRequest.startDate());
        survey.setEndDate(surveyRequest.endDate());
        survey.setStatus(SurveyStatus.DRAFT);

        if(survey.getQuestions() != null) {
            survey.getQuestions().clear();
        }
        if (surveyRequest.questions() != null) {
            for (QuestionRequest qRequest : surveyRequest.questions()) {
                Question question = new Question();
                question.setSurvey(survey);
                question.setQuestionText(qRequest.questionText());
                question.setQuestionType(qRequest.questionType());
                question.setOptions(qRequest.options());
                question.setPosition(qRequest.position());
                survey.getQuestions().add(question);
            }
        }
        return surveyRepository.save(survey);
    }

    private SurveyResponse mapToSurveyResponse(Survey survey) {
        List<QuestionResponse> questionResponses = survey.getQuestions().stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        return new SurveyResponse(
                survey.getId(),
                survey.getName(),
                survey.getType(),
                survey.getUserId(),
                survey.getStatus(),
                survey.getAccessType(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedAt(),
                questionResponses
        );
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getOptions(),
                question.getPosition()
        );
    }
}
