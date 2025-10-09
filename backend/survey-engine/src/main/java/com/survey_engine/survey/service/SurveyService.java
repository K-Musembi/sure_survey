package com.survey_engine.survey.service;

import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.dto.QuestionRequest;
import com.survey_engine.survey.dto.QuestionResponse;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.survey.dto.SurveyRequest;
import com.survey_engine.survey.dto.SurveyResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Survey entity
 * Defines business logic
 */
@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    /**
     * Constructor method
     * @param surveyRepository - survey repository instance
     */
    @Autowired
    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    /**
     * Method to create a new survey
     * @param surveyRequest - request DTO
     * @return - response DTO
     */
    @Transactional
    public SurveyResponse createSurvey(SurveyRequest surveyRequest, String userId) {
        if (surveyRepository.findByName(surveyRequest.name()).isPresent()) {
            throw new DataIntegrityViolationException("A survey with this name already exists");
        }

        Survey survey = new Survey();
        survey.setUserId(userId); // Set the user ID from the authenticated principal
        Survey savedSurvey = getSurvey(survey, surveyRequest);
        return mapToSurveyResponse(savedSurvey);
    }

    /**
     * Method to find survey by id
     * @param id - survey id
     * @return - response DTO
     */
    @Transactional(readOnly = true)
    public SurveyResponse findSurveyById(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));
        return mapToSurveyResponse(survey);
    }

    /**
     * Method to find all surveys for a specific user
     * @param userId - user id
     * @return - list of response DTOs
     */
    @Transactional(readOnly = true)
    public List<SurveyResponse> findSurveysByUserId(String userId) {
        List<Survey> surveys = surveyRepository.findByUserId(userId);
        return surveys.stream()
                .map(this::mapToSurveyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to find all surveys
     * @param roles - roles from JWT
     * @return - list of response DTOs
     */
    @Transactional(readOnly = true)
    public List<SurveyResponse> findAllSurveys(List<String> roles) {
        // Authorization check: only an admin can view all surveys
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("You do not have permission to view all surveys.");
        }
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream()
                .map(this::mapToSurveyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to update survey properties
     * @param id - survey id
     * @param surveyRequest - request DTO
     * @param userId - user id from JWT
     * @param roles - roles from JWT
     * @return - response DTO
     */
    @Transactional
    public SurveyResponse updateSurvey(Long id, SurveyRequest surveyRequest, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));

        // Authorization check: only owner or an admin can update
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this survey.");
        }

        // Business Rule: Core survey properties can only be updated in DRAFT status.
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Survey can only be updated when in DRAFT status.");
        }
        
        Survey savedSurvey = getSurvey(survey, surveyRequest);
        return mapToSurveyResponse(savedSurvey);
    }

    /**
     * Activates a survey, changing its status from DRAFT to ACTIVE.
     * @param surveyId The ID of the survey to activate.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user.
     * @return A response DTO for the activated survey.
     */
    @Transactional
    public SurveyResponse activateSurvey(Long surveyId, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        // Authorization: only owner or admin can activate
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to activate this survey.");
        }

        // Business rule: Can only activate a survey from DRAFT status
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Only surveys in DRAFT status can be activated.");
        }

        // Business rule: Survey must have at least one question to be activated
        if (survey.getQuestions() == null || survey.getQuestions().isEmpty()) {
            throw new IllegalStateException("Survey must have at least one question to be activated.");
        }

        survey.setStatus(SurveyStatus.ACTIVE);
        Survey savedSurvey = surveyRepository.save(survey);
        return mapToSurveyResponse(savedSurvey);
    }

    /**
     * Closes a survey, changing its status from ACTIVE to CLOSED.
     * @param surveyId The ID of the survey to close.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user.
     * @return A response DTO for the closed survey.
     */
    @Transactional
    public SurveyResponse closeSurvey(Long surveyId, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        // Authorization: only owner or admin can close
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to close this survey.");
        }

        // Business rule: Can only close a survey from ACTIVE status
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Only surveys in ACTIVE status can be closed.");
        }

        survey.setStatus(SurveyStatus.CLOSED);
        Survey savedSurvey = surveyRepository.save(survey);
        return mapToSurveyResponse(savedSurvey);
    }

    /**
     * Method to delete survey
     * @param id - survey id
     * @param userId - user id from JWT
     * @param roles - roles from JWT
     */
    @Transactional
    public void deleteSurvey(Long id, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));
        
        // Authorization check: only owner or an admin can delete
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this survey.");
        }

        surveyRepository.delete(survey);
    }

    /**
     * Method to retrieve survey properties and save survey in database
     * @param survey - survey instance
     * @param surveyRequest - request DTO
     * @return - saved survey
     */
    private Survey getSurvey(Survey survey, SurveyRequest surveyRequest) {
        survey.setName(surveyRequest.name());
        survey.setType(surveyRequest.type());
        survey.setAccessType(surveyRequest.accessType());
        survey.setStartDate(surveyRequest.startDate());
        survey.setEndDate(surveyRequest.endDate());
        survey.setStatus(SurveyStatus.DRAFT); // Default status on creation

        // Clear old questions and add new ones
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

    /**
     * Method to map survey to response DTO
     * @param survey - survey instance
     * @return - response DTO
     */
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

    /**
     * Method to map question to response DTO
     * @param question - question instance
     * @return - response DTO
     */
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
