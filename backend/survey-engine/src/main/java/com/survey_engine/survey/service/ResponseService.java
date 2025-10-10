package com.survey_engine.survey.service;

import com.survey_engine.survey.models.Answer;
import com.survey_engine.survey.dto.AnswerResponse;
import com.survey_engine.survey.common.enums.AccessType;
import com.survey_engine.survey.common.enums.ResponseStatus;
import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.config.rabbitmq.RabbitMQConfig;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.models.Response;
import com.survey_engine.survey.repository.QuestionRepository;
import com.survey_engine.survey.repository.ResponseRepository;
import com.survey_engine.survey.dto.ResponseRequest;
import com.survey_engine.survey.dto.ResponseResponse;
import com.survey_engine.survey.dto.ResponseSubmissionPayload;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the Response entity.
 * Defines business logic for submitting and managing survey responses.
 */
@Service
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final RabbitTemplate rabbitTemplate;
    private final QuestionRepository questionRepository;

    /**
     * Constructor for ResponseService.
     *
     * @param responseRepository An instance of ResponseRepository.
     * @param surveyRepository   An instance of SurveyRepository.
     * @param rabbitTemplate     An instance of RabbitTemplate for messaging.
     * @param questionRepository An instance of QuestionRepository
     */
    @Autowired
    public ResponseService(ResponseRepository responseRepository, SurveyRepository surveyRepository, RabbitTemplate rabbitTemplate, QuestionRepository questionRepository) {
        this.responseRepository = responseRepository;
        this.surveyRepository = surveyRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.questionRepository = questionRepository;
    }

    /**
     * Validates and sends a survey response to the message queue for asynchronous processing.
     *
     * @param surveyId The ID of the survey being responded to.
     * @param request  The DTO containing the answers.
     * @param userId   The ID of the user submitting the response (can be null for anonymous).
     */
    public void createResponse(Long surveyId, ResponseRequest request, String userId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Responses can only be submitted to ACTIVE surveys.");
        }
        if (survey.getAccessType() == AccessType.PRIVATE && userId == null) {
            throw new AccessDeniedException("This survey is private and requires authentication to respond.");
        }
        ResponseSubmissionPayload payload = new ResponseSubmissionPayload(surveyId, request, userId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SURVEY_EXCHANGE, RabbitMQConfig.RESPONSE_ROUTING_KEY, payload);
    }

    /**
     * Processes and persists a survey response submission from the message queue.
     * This method is called asynchronously.
     * @param payload The survey submission data from the queue.
     * @return A DTO of the created response.
     */
    @Transactional
    public ResponseResponse handleResponseSubmission(ResponseSubmissionPayload payload) {
        Survey survey = surveyRepository.findById(payload.surveyId())
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + payload.surveyId()));

        Response response = new Response();
        response.setSurvey(survey);
        response.setUserId(payload.userId());
        response.setStatus(ResponseStatus.COMPLETE);
        response.setSubmissionDate(LocalDateTime.now());

        List<Answer> answers = payload.request().answers().stream().map(answerRequest -> {
            Question question = questionRepository.findById(answerRequest.questionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + answerRequest.questionId()));
            Answer answer = new Answer();
            answer.setResponse(response);
            answer.setQuestion(question);
            answer.setAnswerValue(answerRequest.answerValue());
            answer.setPosition(question.getPosition());
            return answer;
        }).toList();

        response.getAnswers().addAll(answers);
        Response savedResponse = responseRepository.save(response);
        return mapToResponseResponse(savedResponse);
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
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

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
                .orElseThrow(() -> new EntityNotFoundException("Response not found with id: " + responseId));

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
                .orElseThrow(() -> new EntityNotFoundException("Response not found with id: " + responseId));

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
        List<AnswerResponse> answerResponses = response.getAnswers().stream()
                .map(answer -> new AnswerResponse(
                        answer.getId(),
                        answer.getQuestion().getId(),
                        answer.getAnswerValue(),
                        answer.getPosition()))
                .collect(Collectors.toList());

        return new ResponseResponse(
                response.getId(),
                response.getSurvey().getId(),
                response.getStatus(),
                response.getSubmissionDate(),
                response.getUserId(),
                answerResponses
        );
    }
}
