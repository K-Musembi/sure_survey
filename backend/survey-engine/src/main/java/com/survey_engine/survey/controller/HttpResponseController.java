package com.survey_engine.survey.controller;

import com.survey_engine.survey.service.ResponseService;
import com.survey_engine.survey.dto.ResponseRequest;
import com.survey_engine.survey.dto.ResponseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling HTTP requests related to survey Responses.
 * Responses are managed as a sub-resource of a Survey.
 */
@RestController
@Validated
@RequestMapping("/api/v1/surveys/{surveyId}/responses")
public class HttpResponseController {

    private final ResponseService responseService;

    /**
     * Constructor for HttpResponseController.
     * @param responseService An instance of ResponseService.
     */
    @Autowired
    public HttpResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    /**
     * Endpoint to submit a new response for a survey.
     * @param surveyId The ID of the survey being responded to.
     * @param request The request body containing the answers.
     * @param jwt The JWT of the authenticated user (if available).
     * @return A ResponseEntity containing the created response.
     */
    @PostMapping
    public ResponseEntity<Void> createResponse(
            @PathVariable Long surveyId,
            @Valid @RequestBody ResponseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        // User can be anonymous (jwt is null) or authenticated
        String userId = (jwt != null) ? jwt.getSubject() : null;
        responseService.createResponse(surveyId, request, userId, null);
        return ResponseEntity.accepted().build();
    }

    /**
     * Endpoint to retrieve all responses for a survey.
     * @param surveyId The ID of the survey.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing a list of responses.
     */
    @GetMapping
    public ResponseEntity<List<ResponseResponse>> getResponsesForSurvey(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<ResponseResponse> responses = responseService.getResponsesBySurveyId(surveyId, userId, roles);
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint to retrieve a single response by its ID.
     * @param surveyId The ID of the survey (for path consistency).
     * @param responseId The ID of the response to retrieve.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the requested response.
     */
    @GetMapping("/{responseId}")
    public ResponseEntity<ResponseResponse> getResponseById(
            @PathVariable Long surveyId,
            @PathVariable Long responseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        ResponseResponse response = responseService.getResponseById(responseId, userId, roles);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to delete a response.
     * @param surveyId The ID of the survey (for path consistency).
     * @param responseId The ID of the response to delete.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{responseId}")
    public ResponseEntity<Void> deleteResponse(
            @PathVariable Long surveyId,
            @PathVariable Long responseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        responseService.deleteResponse(responseId, userId, roles);
        return ResponseEntity.noContent().build();
    }
}
