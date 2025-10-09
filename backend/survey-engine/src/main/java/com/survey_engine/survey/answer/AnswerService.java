package com.survey_engine.survey.answer;

import com.survey_engine.survey.answer.dto.AnswerResponse;
import com.survey.survey.response.Response;
import com.survey.survey.response.ResponseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the Answer entity.
 * Defines business logic for retrieving answer data.
 * Note: Answers are created atomically with a Response and are not managed individually.
 */
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ResponseRepository responseRepository;

    /**
     * Constructor for AnswerService.
     * @param answerRepository An instance of AnswerRepository.
     * @param responseRepository An instance of ResponseRepository.
     */
    @Autowired
    public AnswerService(AnswerRepository answerRepository, ResponseRepository responseRepository) {
        this.answerRepository = answerRepository;
        this.responseRepository = responseRepository;
    }

    /**
     * Retrieves all answers for a given response, with authorization checks.
     * @param responseId The ID of the response.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user.
     * @return A list of AnswerResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByResponseId(Long responseId, String userId, List<String> roles) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> new EntityNotFoundException("Response not found with id: " + responseId));

        // Authorization: Allow survey owner or admin
        if (!response.getSurvey().getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to view these answers.");
        }

        List<Answer> answers = answerRepository.findByResponseId(responseId);
        return answers.stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single answer by its ID, with authorization checks.
     * @param answerId The ID of the answer.
     * @param userId The ID of the user making the request.
     * @param roles The roles of the user.
     * @return An AnswerResponse DTO.
     */
    @Transactional(readOnly = true)
    public AnswerResponse getAnswerById(Long answerId, String userId, List<String> roles) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + answerId));

        // Authorization: Allow survey owner or admin
        if (!answer.getResponse().getSurvey().getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to view this answer.");
        }

        return mapToAnswerResponse(answer);
    }

    /**
     * Maps an Answer entity to an AnswerResponse DTO.
     * @param answer The Answer entity.
     * @return The corresponding AnswerResponse DTO.
     */
    private AnswerResponse mapToAnswerResponse(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getAnswerValue(),
                answer.getPosition()
        );
    }
}
