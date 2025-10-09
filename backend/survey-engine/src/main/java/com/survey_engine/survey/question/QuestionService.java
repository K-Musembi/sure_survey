package com.survey_engine.survey.question;

import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.question.dto.QuestionRequest;
import com.survey_engine.survey.question.dto.QuestionResponse;
import com.survey.survey.survey.Survey;
import com.survey.survey.survey.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Question entity.
 * Defines business logic for managing questions.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SurveyRepository surveyRepository;

    /**
     * Constructor for QuestionService.
     * @param questionRepository - An instance of QuestionRepository.
     * @param surveyRepository - An instance of SurveyRepository.
     */
    @Autowired
    public QuestionService(QuestionRepository questionRepository, SurveyRepository surveyRepository) {
        this.questionRepository = questionRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * Creates a new question for a given survey.
     * @param surveyId The ID of the survey to add the question to.
     * @param questionRequest The request DTO containing question data.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user performing the action.
     * @return A response DTO for the created question.
     */
    @Transactional
    public QuestionResponse createQuestion(Long surveyId, QuestionRequest questionRequest, String userId, List<String> roles) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        // Authorization check: only owner or an admin can add a question
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to add questions to this survey.");
        }

        // Business rule: Questions can only be added to surveys in DRAFT status.
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Questions can only be added to surveys in DRAFT status.");
        }

        Question question = new Question();
        question.setSurvey(survey);
        question.setQuestionText(questionRequest.questionText());
        question.setQuestionType(questionRequest.questionType());
        question.setOptions(questionRequest.options());
        question.setPosition(questionRequest.position());

        Question savedQuestion = questionRepository.save(question);
        return mapToQuestionResponse(savedQuestion);
    }

    /**
     * Retrieves all questions for a given survey.
     * @param surveyId The ID of the survey.
     * @return A list of response DTOs for the questions.
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsBySurveyId(Long surveyId) {
        if (!surveyRepository.existsById(surveyId)) {
            throw new EntityNotFoundException("Survey not found with id: " + surveyId);
        }
        List<Question> questions = questionRepository.findBySurveyId(surveyId);
        return questions.stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single question by its ID.
     * @param questionId The ID of the question.
     * @return A response DTO for the question.
     */
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));
        return mapToQuestionResponse(question);
    }

    /**
     * Updates an existing question.
     * @param questionId The ID of the question to update.
     * @param questionRequest The request DTO with updated data.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user performing the action.
     * @return A response DTO for the updated question.
     */
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest questionRequest, String userId, List<String> roles) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        Survey survey = question.getSurvey();
        // Authorization check: only owner or an admin can update
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this question.");
        }

        // Business rule: Questions can only be modified on surveys in DRAFT status.
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Questions can only be modified on surveys in DRAFT status.");
        }

        question.setQuestionText(questionRequest.questionText());
        question.setQuestionType(questionRequest.questionType());
        question.setOptions(questionRequest.options());
        question.setPosition(questionRequest.position());

        Question updatedQuestion = questionRepository.save(question);
        return mapToQuestionResponse(updatedQuestion);
    }

    /**
     * Deletes a question by its ID.
     * @param questionId The ID of the question to delete.
     * @param userId The ID of the user performing the action.
     * @param roles The roles of the user performing the action.
     */
    @Transactional
    public void deleteQuestion(Long questionId, String userId, List<String> roles) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        Survey survey = question.getSurvey();
        // Authorization check: only owner or an admin can delete
        if (!survey.getUserId().equals(userId) && (roles == null || !roles.contains("ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this question.");
        }

        // Business rule: Questions can only be deleted from surveys in DRAFT status.
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Questions can only be deleted from surveys in DRAFT status.");
        }

        questionRepository.delete(question);
    }

    /**
     * Maps a Question entity to a QuestionResponse DTO.
     * @param question The Question entity.
     * @return The QuestionResponse DTO.
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
