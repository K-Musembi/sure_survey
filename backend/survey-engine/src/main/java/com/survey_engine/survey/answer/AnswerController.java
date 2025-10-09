package com.survey_engine.survey.answer;

import com.survey_engine.survey.answer.dto.AnswerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for handling HTTP requests related to Answers.
 * Answers are retrieved as a sub-resource of a Response.
 */
@RestController
@Validated
@RequestMapping("/api/v1/responses/{responseId}/answers")
public class AnswerController {

    private final AnswerService answerService;

    /**
     * Constructor for AnswerController.
     * @param answerService An instance of AnswerService.
     */
    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    /**
     * Endpoint to retrieve all answers for a specific response.
     * @param responseId The ID of the response.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing a list of answers.
     */
    @GetMapping
    public ResponseEntity<List<AnswerResponse>> getAnswersForResponse(
            @PathVariable Long responseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<AnswerResponse> answers = answerService.getAnswersByResponseId(responseId, userId, roles);
        return ResponseEntity.ok(answers);
    }

    /**
     * Endpoint to retrieve a single answer by its ID.
     * @param responseId The ID of the response (for path consistency).
     * @param answerId The ID of the answer to retrieve.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the requested answer.
     */
    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(
            @PathVariable Long responseId, // Used for context, but logic relies on answerId
            @PathVariable Long answerId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        AnswerResponse answer = answerService.getAnswerById(answerId, userId, roles);
        return ResponseEntity.ok(answer);
    }
}
