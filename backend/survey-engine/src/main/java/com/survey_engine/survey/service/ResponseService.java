package com.survey_engine.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.billing.BillingApi;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.survey.dto.ResponseRequest;
import com.survey_engine.survey.dto.ResponseResponse;
import com.survey_engine.survey.models.Answer;
import com.survey_engine.survey.dto.AnswerResponse;
import com.survey_engine.survey.common.enums.AccessType;
import com.survey_engine.survey.common.enums.ResponseStatus;
import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.dto.ResponseSubmissionPayload;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.models.Response;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.QuestionRepository;
import com.survey_engine.survey.repository.ResponseRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.user.UserApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for the Response entity.
 * Defines business logic for submitting and managing survey responses.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final ResponseRabbitMqPublisher responseRabbitMqPublisher;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserApi userApi;
    private final BillingApi billingApi;
    private final ObjectMapper objectMapper;
    private final BranchRuleService branchRuleService;
    private final SystemSettingRepository systemSettingRepository;


    /**
     * Create survey response instance with optional metadata.
     * @param surveyId - id of survey
     * @param responseRequest - ResponseRequest DTO
     * @param userId - id of user
     * @param sessionId - session id
     * @param metadata - map of contextual data (e.g. attribution)
     */
    public void createResponse(Long surveyId, ResponseRequest responseRequest, String userId, String sessionId, Map<String, String> metadata) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("SURVEY_NOT_FOUND", "Survey not found with id: " + surveyId));

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new BusinessRuleException("SURVEY_NOT_ACTIVE", "Responses can only be submitted to ACTIVE surveys.");
        }

        // Validate response limits based on subscription
        billingApi.validateResponseLimit(survey.getTenantId(), surveyId);

        // Validate that the response channel is allowed by the survey owner's plan
        String channel = metadata != null ? metadata.get("channel") : null;
        if (channel != null) {
            billingApi.validateChannelAllowed(survey.getTenantId(),
                    parseLongSafe(survey.getUserId()), channel);
        }

        if (survey.getAccessType() == AccessType.PRIVATE && userId == null) {
            throw new AccessDeniedException("This survey is private and requires authentication to respond.");
        }

        // Enforce configured target limits (if any)
        if (survey.getTargetRespondents() != null && survey.getTargetRespondents() > 0) {
            long currentResponseCount = responseRepository.countBySurveyId(surveyId);
            if (currentResponseCount >= survey.getTargetRespondents()) {
                throw new BusinessRuleException("TARGET_RESPONDENTS_REACHED", "This survey has reached its maximum number of responses.");
            }
        }

        ResponseSubmissionPayload payload = new ResponseSubmissionPayload(surveyId, responseRequest, userId, sessionId, metadata);
        responseRabbitMqPublisher.publishResponse(payload);
    }

    @Transactional
    public ResponseResponse handleResponseSubmissionAndRewardPublishing(ResponseSubmissionPayload payload) {
        Survey survey = surveyRepository.findById(payload.surveyId())
                .orElseThrow(() -> new ResourceNotFoundException("SURVEY_NOT_FOUND", "Survey not found with id: " + payload.surveyId()));

        Response response = new Response();
        response.setSurvey(survey);
        response.setTenantId(survey.getTenantId());
        response.setParticipantId(payload.participantId());
        response.setSessionId(payload.sessionId()); // Set the session ID
        response.setStatus(ResponseStatus.COMPLETE);
        response.setSubmissionDate(LocalDateTime.now());
        
        // Save metadata if present
        if (payload.metadata() != null && !payload.metadata().isEmpty()) {
            try {
                response.setMetadata(objectMapper.writeValueAsString(payload.metadata()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize metadata for response to survey {}", payload.surveyId(), e);
            }
        }

        List<Answer> answers = payload.request().answers().stream().map(answerRequest -> {
            Question question = questionRepository.findById(answerRequest.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("QUESTION_NOT_FOUND", "Question not found with id: " + answerRequest.questionId()));
            Answer answer = new Answer();
            answer.setResponse(response);
            answer.setQuestion(question);
            answer.setAnswerValue(answerRequest.answerValue());
            answer.setPosition(question.getPosition());
            return answer;
        }).toList();

        response.getAnswers().addAll(answers);
        Response savedResponse = responseRepository.save(response);

        // Debit wallet for SMS/WhatsApp channel responses
        debitChannelCost(survey, payload);

        // Evaluate branch rules if answers are present
        Long nextQuestionId = evaluateBranchRules(survey.getId(), savedResponse);

        String responderId = null;

        // If it's an SMS response, the responderId is the phone number.
        // We also try to link it to a participant if one exists for data consistency.
        if (savedResponse.getSessionId() != null) {
            responderId = savedResponse.getSessionId();
            userApi.findParticipantIdByPhoneNumber(responderId).ifPresent(participantId -> {
                savedResponse.setParticipantId(participantId);
                responseRepository.save(savedResponse);
            });
        }
        // If it's a web response from a user who opted into rewards, the participantId (participantId) is the responderId.
        else if (savedResponse.getParticipantId() != null) {
            responderId = savedResponse.getParticipantId();
        }

        // Publish the completion event if we have a responderId.
        // This occurs for all SMS responses and for web responses where the user opted-in for a reward.
        if (responderId != null) {
            SurveyCompletedEvent event = new SurveyCompletedEvent(
                    savedResponse.getSurvey().getId(),
                    savedResponse.getId(),
                    responderId
            );
            eventPublisher.publishEvent(event);
        }

        return mapToResponseResponseWithNext(savedResponse, nextQuestionId);
    }

    /**
     * Debits the survey owner's wallet for per-response channel costs (SMS/WhatsApp).
     * Web/USSD responses have no per-message cost.
     */
    private void debitChannelCost(Survey survey, ResponseSubmissionPayload payload) {
        String channel = payload.metadata() != null ? payload.metadata().get("channel") : null;
        if (channel == null) return;

        SettingKey costKey;
        if ("SMS".equalsIgnoreCase(channel)) {
            costKey = SettingKey.SMS_COST_PER_MESSAGE;
        } else if ("WHATSAPP".equalsIgnoreCase(channel)) {
            costKey = SettingKey.WHATSAPP_COST_PER_MESSAGE;
        } else {
            return; // Web/USSD — no per-message cost
        }

        if (survey.getUserId() == null) return;

        try {
            systemSettingRepository.findByKey(costKey).ifPresent(setting -> {
                java.math.BigDecimal cost = new java.math.BigDecimal(setting.getValue());
                if (cost.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    billingApi.debitWallet(survey.getTenantId(), parseLongSafe(survey.getUserId()),
                            cost, channel + " response cost for survey " + survey.getId());
                    log.info("Debited {} KES for {} response on survey {}", cost, channel, survey.getId());
                }
            });
        } catch (Exception e) {
            // Log but don't fail the response submission — wallet debit is best-effort
            log.error("Failed to debit channel cost for {} response on survey {}: {}",
                    channel, survey.getId(), e.getMessage());
        }
    }

    /**
     * Evaluates branch rules for the last answered question and computes the next question ID.
     * Returns: specific question ID, null (end survey), or -1L sentinel (linear progression).
     */
    private Long evaluateBranchRules(Long surveyId, Response response) {
        if (response.getAnswers().isEmpty()) return -1L;

        // Use the last answer to evaluate branch rules
        Answer lastAnswer = response.getAnswers().get(response.getAnswers().size() - 1);
        Question lastQuestion = lastAnswer.getQuestion();

        // Determine selected option index (for multiple choice questions)
        Integer selectedOptionIndex = null;
        try {
            selectedOptionIndex = Integer.parseInt(lastAnswer.getAnswerValue());
        } catch (NumberFormatException e) {
            // Not a numeric answer — branch rules requiring optionIndex won't match
        }

        // Accumulate scores from all answers in this response
        double totalScore = 0.0;
        Map<String, Double> categoryScores = new java.util.HashMap<>();

        for (Answer answer : response.getAnswers()) {
            Question q = answer.getQuestion();
            if (q.getScoreMap() != null && !q.getScoreMap().isBlank()) {
                try {
                    Map<String, Double> scoreMap = objectMapper.readValue(q.getScoreMap(),
                            new TypeReference<Map<String, Double>>() {});
                    Double score = scoreMap.get(answer.getAnswerValue());
                    if (score != null) {
                        double weighted = score * (q.getWeight() != null ? q.getWeight() : 1.0);
                        totalScore += weighted;
                        if (q.getCategory() != null) {
                            categoryScores.merge(q.getCategory(), weighted, Double::sum);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse scoreMap for question {}: {}", q.getId(), e.getMessage());
                }
            }
        }

        return branchRuleService.resolveNextQuestion(surveyId, lastQuestion.getId(),
                selectedOptionIndex, categoryScores, totalScore);
    }

    /**
     * Retrieves all responses for a specific survey.
     * Only the survey owner or an admin can perform this action.
     *
     * @param surveyId The ID of the survey.
     * @param userId   The ID of the user making the request.
     * @param roles    The roles of the user.
     * @return A list of response DTOs.
     */
    @Transactional(readOnly = true)
    public List<ResponseResponse> getResponsesBySurveyId(Long surveyId, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("RESOURCE_NOT_FOUND","Survey not found with id: " + surveyId));

        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to view responses for this survey.");
        }

        List<Response> responses = responseRepository.findBySurveyId(surveyId);
        return responses.stream()
                .map(this::mapToResponseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single response by its ID.
     * Only the survey owner or an admin can perform this action.
     *
     * @param responseId The ID of the response.
     * @param userId     The ID of the user making the request.
     * @param roles      The roles of the user.
     * @return A response DTO.
     */
    @Transactional(readOnly = true)
    public ResponseResponse getResponseById(Long responseId, String userId, List<String> roles) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("RESOURCE_NOT_FOUND","Response not found with id: " + responseId));

        if (!response.getSurvey().getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to view this response.");
        }

        return mapToResponseResponse(response);
    }

    /**
     * Deletes a response.
     * Only the survey owner or an admin can perform this action.
     *
     * @param responseId The ID of the response to delete.
     * @param userId     The ID of the user making the request.
     * @param roles      The roles of the user.
     */
    @Transactional
    public void deleteResponse(Long responseId, String userId, List<String> roles) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("RESOURCE_NOT_FOUND","Response not found with id: " + responseId));

        if (!response.getSurvey().getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this response.");
        }

        responseRepository.delete(response);
    }

    /**
     * Maps a Response entity to a ResponseResponse DTO.
     *
     * @param response The Response entity.
     * @return The corresponding ResponseResponse DTO.
     */
    private ResponseResponse mapToResponseResponse(Response response) {
        return mapToResponseResponseWithNext(response, null);
    }

    private ResponseResponse mapToResponseResponseWithNext(Response response, Long nextQuestionId) {
        List<AnswerResponse> answerResponses = response.getAnswers().stream()
                .map(answer -> new AnswerResponse(
                        answer.getId(),
                        answer.getQuestion().getId(),
                        answer.getAnswerValue(),
                        answer.getPosition()))
                .collect(Collectors.toList());

        // -1L sentinel means "no branch rule matched, use linear progression" — translate to null for API
        Long resolvedNext = (nextQuestionId != null && nextQuestionId == -1L) ? null : nextQuestionId;

        return new ResponseResponse(
                response.getId(),
                response.getSurvey().getId(),
                response.getStatus(),
                response.getSubmissionDate(),
                response.getParticipantId(),
                answerResponses,
                resolvedNext
        );
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse userId as Long: {}", value);
            return null;
        }
    }
}