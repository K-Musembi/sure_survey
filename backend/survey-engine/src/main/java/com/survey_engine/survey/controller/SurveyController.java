package com.survey_engine.survey.controller;

import com.survey_engine.survey.service.SurveyService;
import com.survey_engine.survey.dto.SurveyRequest;
import com.survey_engine.survey.dto.SurveyResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;

/**
 * Controller class for survey entity
 * HTTP requests and responses
 */
@RestController
@Validated
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * Constructor for controller class
     * @param surveyService - instance of service class
     */
    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * Method to create new survey for the authenticated user.
     * @param surveyRequest - request DTO
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping
    public ResponseEntity<SurveyResponse> createSurvey(@Valid @RequestBody SurveyRequest surveyRequest, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        SurveyResponse responseObject = surveyService.createSurvey(surveyRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Method to retrieve survey by id
     * @param id - survey id
     * @return - HTTP response
     */
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponse> getSurveyById(@PathVariable Long id) {
        SurveyResponse responseObject = surveyService.findSurveyById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve all surveys for the currently authenticated user.
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @GetMapping("/my-surveys")
    public ResponseEntity<List<SurveyResponse>> getMySurveys(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<SurveyResponse> responseObject = surveyService.findSurveysByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve all surveys (admin use)
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @GetMapping
    public ResponseEntity<List<SurveyResponse>> getAllSurveys(@AuthenticationPrincipal Jwt jwt) throws AuthenticationException {
        if (jwt == null) {
            throw new AuthenticationException("User is not authenticated.");
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<SurveyResponse> responseObject = surveyService.findAllSurveys(roles);
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
    public ResponseEntity<SurveyResponse> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyRequest surveyRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        // Assuming roles are in a JWT 'roles' claim, e.g., ["USER", "ADMIN"]
        List<String> roles = jwt.getClaimAsStringList("roles"); 
        SurveyResponse responseObject = surveyService.updateSurvey(id, surveyRequest, userId, roles);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to activate a survey.
     * @param id - survey id
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<SurveyResponse> activateSurvey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        SurveyResponse responseObject = surveyService.activateSurvey(id, userId, roles);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * Method to close a survey.
     * @param id - survey id
     * @param jwt - The JWT token of the authenticated user
     * @return - HTTP response
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<SurveyResponse> closeSurvey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        SurveyResponse responseObject = surveyService.closeSurvey(id, userId, roles);
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
}
