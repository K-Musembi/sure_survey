package com.survey_engine.survey.service;

import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.dto.QuestionRequest;
import com.survey_engine.survey.dto.QuestionResponse;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.survey.dto.SurveyRequest;
import com.survey_engine.survey.dto.SurveysResponse;
import com.survey_engine.user.UserApi;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing surveys.
 * Handles business logic related to survey creation, retrieval, updates, and status changes.
 */
@Service
@RequiredArgsConstructor
public class SurveyService {

    private static final Logger logger = LoggerFactory.getLogger(SurveyService.class);

    private final SurveyRepository surveyRepository;
    private final UserApi userApi;

    /**
     * Creates a new survey for the authenticated user.
     *
     * @param surveyRequest The request DTO containing survey details.
     * @param userId The ID of the user creating the survey.
     * @return A {@link SurveysResponse} DTO for the newly created survey.
     * @throws IllegalStateException if the tenant context is not found.
     * @throws DataIntegrityViolationException if a survey with the same name already exists for the tenant.
     */
    @Transactional
    public SurveysResponse createSurvey(SurveyRequest surveyRequest, String userId) {
        Long tenantId = userApi.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found. Cannot create survey without a tenant.");
        }
        if (surveyRepository.findByNameAndTenantId(surveyRequest.name(), tenantId).isPresent()) {
            throw new DataIntegrityViolationException("A survey with this name already exists");
        }

        Survey survey = new Survey();
        survey.setTenantId(tenantId);
        survey.setUserId(userId);
        Survey savedSurvey = getSurvey(survey, surveyRequest);

        String userName = userApi.getUserNameById(userId);
        Map<String, String> userIdToNameMap = Collections.singletonMap(userId, userName);

        return mapToSurveyResponse(savedSurvey, userIdToNameMap);
    }

    /**
     * Finds a single survey by its ID within the current tenant.
     *
     * @param id The ID of the survey to find.
     * @return A {@link SurveysResponse} for the found survey.
     * @throws EntityNotFoundException if the survey is not found.
     */
    @Transactional(readOnly = true)
    public SurveysResponse findSurveyById(Long id) {
        Long tenantId = userApi.getTenantId();
        Survey survey = surveyRepository.findById(id)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + id));

        String userName = userApi.getUserNameById(survey.getUserId());
        Map<String, String> userIdToNameMap = Collections.singletonMap(survey.getUserId(), userName);

        return mapToSurveyResponse(survey, userIdToNameMap);
    }

    /**
     * Finds all surveys created by the specified user.
     *
     * @param userId The ID of the user.
     * @return A list of {@link SurveysResponse} objects.
     */
    @Transactional(readOnly = true)
    public List<SurveysResponse> findMySurveys(String userId) {
        Long tenantId = userApi.getTenantId();
        List<Survey> surveys = surveyRepository.findByTenantIdAndUserIdIn(tenantId, Collections.singletonList(userId));
        return getSurveyResponses(surveys, Collections.singleton(userId));
    }

    /**
     * Finds all surveys belonging to the department of the specified user.
     *
     * @param userId The ID of the user whose department surveys are to be fetched.
     * @return A list of {@link SurveysResponse} objects for the department.
     */
    @Transactional(readOnly = true)
    public List<SurveysResponse> findMyTeamSurveys(String userId) {
        Long tenantId = userApi.getTenantId();
        String currentUserDepartment = userApi.getUserDepartmentById(userId);

        if (currentUserDepartment == null || currentUserDepartment.isEmpty()) {
            return Collections.emptyList(); // Or handle as an error, depending on requirements
        }

        List<String> departmentUserIds = userApi.getUserIdsByTenantIdAndDepartment(tenantId, currentUserDepartment);
        Set<String> userIdsToFetch = new HashSet<>(departmentUserIds);

        List<Survey> surveys = surveyRepository.findByTenantIdAndUserIdIn(tenantId, new ArrayList<>(userIdsToFetch));

        return getSurveyResponses(surveys, userIdsToFetch);
    }


    /**
     * Finds all surveys within the current tenant. This method is intended for ADMIN users.
     *
     * @param roles The roles of the user requesting the data.
     * @return A list of all {@link SurveysResponse} objects for the tenant.
     * @throws AccessDeniedException if the user is not an ADMIN.
     */
    @Transactional(readOnly = true)
    public List<SurveysResponse> findAllSurveys(List<String> roles) {
        Long tenantId = userApi.getTenantId();

        if (roles == null || !roles.contains("ADMIN"))  {
            throw new AccessDeniedException("You are not allowed to view all surveys");
        }

        // For ADMINs, fetch all surveys within their tenant
        List<Survey> surveys = surveyRepository.findByTenantId(tenantId);

        Set<String> uniqueUserIds = surveys.stream()
                .map(Survey::getUserId)
                .collect(Collectors.toSet());

        return getSurveyResponses(surveys, uniqueUserIds);
    }

    /**
     * Updates an existing survey.
     *
     * @param id The ID of the survey to update.
     * @param surveyRequest The request DTO with updated survey details.
     * @param userId The ID of the user performing the update.
     * @param roles The roles of the user.
     * @return A {@link SurveysResponse} for the updated survey.
     * @throws EntityNotFoundException if the survey is not found.
     * @throws AccessDeniedException if the user does not have permission.
     * @throws IllegalStateException if the survey is not in DRAFT status.
     */
    @Transactional
    public SurveysResponse updateSurvey(Long id, SurveyRequest surveyRequest, String userId, List<String> roles) {
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

        String userName = userApi.getUserNameById(userId);
        Map<String, String> userIdToNameMap = Collections.singletonMap(userId, userName);

        return mapToSurveyResponse(savedSurvey, userIdToNameMap);
    }

    /**
     * Activates a survey, changing its status from DRAFT to ACTIVE.
     *
     * @param surveyId The ID of the survey to activate.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user.
     * @return A {@link SurveysResponse} for the activated survey.
     * @throws EntityNotFoundException if the survey is not found.
     * @throws AccessDeniedException if the user does not have permission.
     * @throws IllegalStateException if the survey is not in DRAFT status or has no questions.
     */
    @Transactional
    public SurveysResponse activateSurvey(Long surveyId, String userId, List<String> roles) {
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
        return getSurveyResponse(userId, survey);
    }

    /**
     * Closes a survey, changing its status from ACTIVE to CLOSED.
     *
     * @param surveyId The ID of the survey to close.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user.
     * @return A {@link SurveysResponse} for the closed survey.
     * @throws EntityNotFoundException if the survey is not found.
     * @throws AccessDeniedException if the user does not have permission.
     * @throws IllegalStateException if the survey is not in ACTIVE status.
     */
    @Transactional
    public SurveysResponse closeSurvey(Long surveyId, String userId, List<String> roles) {
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
        return getSurveyResponse(userId, survey);
    }

    /**
     * Activates a survey that has been successfully paid for.
     * This method is typically called by an event listener after a payment event.
     *
     * @param surveyId The ID of the survey to activate.
     * @throws EntityNotFoundException if the survey is not found.
     */
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
        logger.info("Successfully activated survey after payment.");

        String userName = userApi.getUserNameById(survey.getUserId());
        Map<String, String> userIdToNameMap = Collections.singletonMap(survey.getUserId(), userName);

        mapToSurveyResponse(survey, userIdToNameMap);
    }

    /**
     * Deletes a survey.
     *
     * @param id The ID of the survey to delete.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user.
     * @throws EntityNotFoundException if the survey is not found.
     * @throws AccessDeniedException if the user does not have permission.
     */
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

    /**
     * Saves a survey and returns its corresponding response DTO.
     *
     * @param userId The ID of the user associated with the survey action.
     * @param survey The {@link Survey} entity to save.
     * @return A {@link SurveysResponse} for the saved survey.
     */
    @NotNull
    private SurveysResponse getSurveyResponse(String userId, Survey survey) {
        Survey savedSurvey = surveyRepository.save(survey);

        String userName = userApi.getUserNameById(userId);
        Map<String, String> userIdToNameMap = Collections.singletonMap(userId, userName);

        return mapToSurveyResponse(savedSurvey, userIdToNameMap);
    }

    /**
     * Converts a list of Survey entities to a list of SurveyResponse DTOs.
     *
     * @param surveys The list of {@link Survey} entities.
     * @param uniqueUserIds A set of user IDs for which to fetch usernames.
     * @return A list of {@link SurveysResponse} DTOs.
     */
    @NotNull
    private List<SurveysResponse> getSurveyResponses(List<Survey> surveys, Set<String> uniqueUserIds) {
        Map<String, String> userIdToNameMap = userApi.getUserNamesByIds(uniqueUserIds);

        return surveys.stream()
                .map(survey -> mapToSurveyResponse(survey, userIdToNameMap))
                .collect(Collectors.toList());
    }

    /**
     * Populates a Survey entity from a SurveyRequest DTO and saves it.
     *
     * @param survey The {@link Survey} entity to populate.
     * @param surveyRequest The {@link SurveyRequest} DTO containing the data.
     * @return The saved {@link Survey} entity.
     */
    private Survey getSurvey(Survey survey, SurveyRequest surveyRequest) {
        survey.setName(surveyRequest.name());
        survey.setIntroduction(surveyRequest.introduction());
        survey.setType(surveyRequest.type());
        survey.setAccessType(surveyRequest.accessType());
        survey.setStartDate(surveyRequest.startDate());
        survey.setEndDate(surveyRequest.endDate());
        survey.setStatus(SurveyStatus.DRAFT);

        if(survey.getQuestions() != null) {
            survey.getQuestions().clear();
        }
        if (surveyRequest.questions() != null) {
            for (QuestionRequest questionRequest : surveyRequest.questions()) {
                Question question = new Question();
                question.setSurvey(survey);
                question.setQuestionText(questionRequest.questionText());
                question.setQuestionType(questionRequest.questionType());
                question.setOptions(questionRequest.options());
                question.setPosition(questionRequest.position());
                survey.getQuestions().add(question);
            }
        }
        return surveyRepository.save(survey);
    }

    /**
     * Maps a Survey entity to a SurveyResponse DTO.
     *
     * @param survey The {@link Survey} entity to map.
     * @param userIdToNameMap A map of user IDs to usernames.
     * @return A {@link SurveysResponse} DTO.
     */
    private SurveysResponse mapToSurveyResponse(Survey survey, Map<String, String> userIdToNameMap) {
        List<QuestionResponse> questions = survey.getQuestions().stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        String createdByName = userIdToNameMap.get(survey.getUserId());

        return new SurveysResponse(
                survey.getId(),
                survey.getName(),
                survey.getIntroduction(),
                survey.getType(),
                survey.getUserId(),
                createdByName,
                survey.getStatus(),
                survey.getAccessType(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedAt(),
                questions
        );
    }

    /**
     * Maps a Question entity to a QuestionResponse DTO.
     *
     * @param question The {@link Question} entity to map.
     * @return A {@link QuestionResponse} DTO.
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
