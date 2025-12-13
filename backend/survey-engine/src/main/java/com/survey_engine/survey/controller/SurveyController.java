package com.survey_engine.survey.controller;

import com.survey_engine.survey.service.SurveyService;
import com.survey_engine.survey.dto.SurveyRequest;
import com.survey_engine.survey.dto.SurveysResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for survey entity
 * HTTP requests and responses
 */
@RestController
@Validated
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * Method to create new survey for the authenticated user.
     * @param surveyRequest - request DTO
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping
    public ResponseEntity<SurveysResponse> createSurvey(@Valid @RequestBody SurveyRequest surveyRequest, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        SurveysResponse responseObject = surveyService.createSurvey(surveyRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Method to retrieve survey by id
     * @param id - survey id
     * @return - HTTP response
     */
    @GetMapping("/{id}")
    public ResponseEntity<SurveysResponse> getSurveyById(@PathVariable Long id) {
        SurveysResponse responseObject = surveyService.findSurveyById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Retrieves all surveys created by the currently authenticated user.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return A ResponseEntity containing a list of the user's surveys.
     */
    @GetMapping("/my-surveys")
    public ResponseEntity<List<SurveysResponse>> getMySurveys(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<SurveysResponse> responseObject = surveyService.findMySurveys(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Retrieves all surveys belonging to the department of the currently authenticated user.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return A ResponseEntity containing a list of the team's surveys.
     */
    @GetMapping("/my-team")
    public ResponseEntity<List<SurveysResponse>> getMyTeamSurveys(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<SurveysResponse> responseObject = surveyService.findMyTeamSurveys(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve all surveys (admin use)
     * @param jwt - The JWT token of the authenticated user
     *
     */
    @GetMapping
    public ResponseEntity<List<SurveysResponse>> getAllSurveys(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<SurveysResponse> responseObject = surveyService.findAllSurveys(roles);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to update survey details
     * @param id - survey id
     * @param surveyRequest - request DTO
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PutMapping("/{id}")
    public ResponseEntity<SurveysResponse> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyRequest surveyRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        // Assuming roles are in a JWT 'roles' claim, e.g., ["USER", "ADMIN"]
        List<String> roles = jwt.getClaimAsStringList("roles"); 
        SurveysResponse responseObject = surveyService.updateSurvey(id, surveyRequest, userId, roles);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to activate a survey.
     * @param id - survey id
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<SurveysResponse> activateSurvey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        SurveysResponse responseObject = surveyService.activateSurvey(id, userId, roles);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * Method to close a survey.
     * @param id - survey id
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<SurveysResponse> closeSurvey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        SurveysResponse responseObject = surveyService.closeSurvey(id, userId, roles);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * Method to delete survey
     * @param id - survey id
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        surveyService.deleteSurvey(id, userId, roles);
        return ResponseEntity.noContent().build();
    }

    /**
     * Triggers sending the survey to all contacts in its linked distribution list.
     * @param id The ID of the survey.
     * @param jwt The JWT token of the authenticated user.
     * @return A ResponseEntity with Accepted status.
     */
    @PostMapping("/{id}/send-to-distribution-list")
    public ResponseEntity<Void> sendSurveyToDistributionList(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        surveyService.sendSurveyToDistributionList(id, userId, roles);
        return ResponseEntity.accepted().build();
    }
}
