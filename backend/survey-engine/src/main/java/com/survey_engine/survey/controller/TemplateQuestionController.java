package com.survey_engine.survey.controller;

import com.survey_engine.survey.service.TemplateQuestionService;
import com.survey_engine.survey.dto.TemplateQuestionRequest;
import com.survey_engine.survey.dto.TemplateQuestionResponse;
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
 * Controller for handling HTTP requests related to TemplateQuestions.
 */
@RestController
@Validated
@RequestMapping("/api/v1/templates/{templateId}/questions")
public class TemplateQuestionController {

    private final TemplateQuestionService questionService;

    /**
     * Constructor for TemplateQuestionController.
     * @param questionService An instance of TemplateQuestionService.
     */
    @Autowired
    public TemplateQuestionController(TemplateQuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Endpoint to create a new question for a template. (Admin only)
     * @param templateId The ID of the template.
     * @param request The request body with question data.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with the created question.
     */
    @PostMapping
    public ResponseEntity<TemplateQuestionResponse> createTemplateQuestion(
            @PathVariable Long templateId,
            @Valid @RequestBody TemplateQuestionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        TemplateQuestionResponse createdQuestion = questionService.createTemplateQuestion(templateId, request, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    /**
     * Endpoint to retrieve all questions for a specific template.
     * @param templateId The ID of the template.
     * @return A ResponseEntity with a list of questions.
     */
    @GetMapping
    public ResponseEntity<List<TemplateQuestionResponse>> getQuestionsForTemplate(@PathVariable Long templateId) {
        List<TemplateQuestionResponse> questions = questionService.getQuestionsByTemplateId(templateId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Endpoint to retrieve a specific question by its ID.
     * @param templateId The ID of the template (for path consistency).
     * @param questionId The ID of the question.
     * @return A ResponseEntity with the requested question.
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<TemplateQuestionResponse> getQuestionById(
            @PathVariable Long templateId,
            @PathVariable Long questionId) {
        TemplateQuestionResponse question = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(question);
    }

    /**
     * Endpoint to update an existing template question. (Admin only)
     * @param templateId The ID of the template (for path consistency).
     * @param questionId The ID of the question to update.
     * @param request The request body with updated data.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with the updated question.
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<TemplateQuestionResponse> updateTemplateQuestion(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @Valid @RequestBody TemplateQuestionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        TemplateQuestionResponse updatedQuestion = questionService.updateTemplateQuestion(questionId, request, roles);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * Endpoint to delete a template question. (Admin only)
     * @param templateId The ID of the template (for path consistency).
     * @param questionId The ID of the question to delete.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteTemplateQuestion(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        questionService.deleteTemplateQuestion(questionId, roles);
        return ResponseEntity.noContent().build();
    }
}
