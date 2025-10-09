package com.survey_engine.survey.controller;

import com.survey_engine.survey.service.QuestionService;
import com.survey_engine.survey.dto.QuestionRequest;
import com.survey_engine.survey.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling HTTP requests related to Questions.
 * Questions are managed as a sub-resource of a Survey.
 */
@RestController
@Validated
@RequestMapping("/api/v1/surveys/{surveyId}/questions")
public class QuestionController {

    private final QuestionService questionService;

    /**
     * Constructor for QuestionController.
     * @param questionService An instance of QuestionService.
     */
    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Endpoint to create a new question for a survey.
     * @param surveyId The ID of the survey.
     * @param questionRequest The request body containing question details.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the created question and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @PathVariable Long surveyId,
            @Valid @RequestBody QuestionRequest questionRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        QuestionResponse createdQuestion = questionService.createQuestion(surveyId, questionRequest, userId, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    /**
     * Endpoint to retrieve all questions for a specific survey.
     * @param surveyId The ID of the survey.
     * @return A ResponseEntity containing a list of questions and HTTP status 200.
     */
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestionsForSurvey(@PathVariable Long surveyId) {
        List<QuestionResponse> questions = questionService.getQuestionsBySurveyId(surveyId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Endpoint to retrieve a specific question by its ID.
     * @param surveyId The ID of the survey (for path consistency).
     * @param questionId The ID of the question.
     * @return A ResponseEntity containing the question and HTTP status 200.
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long surveyId, @PathVariable Long questionId) {
        QuestionResponse question = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(question);
    }

    /**
     * Endpoint to update an existing question.
     * @param surveyId The ID of the survey (for path consistency).
     * @param questionId The ID of the question to update.
     * @param questionRequest The request body with updated question details.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the updated question and HTTP status 200.
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest questionRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        QuestionResponse updatedQuestion = questionService.updateQuestion(questionId, questionRequest, userId, roles);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * Endpoint to delete a question.
     * @param surveyId The ID of the survey (for path consistency).
     * @param questionId The ID of the question to delete.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with HTTP status 204 (No Content).
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        questionService.deleteQuestion(questionId, userId, roles);
        return ResponseEntity.noContent().build();
    }
}
