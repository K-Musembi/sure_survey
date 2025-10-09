package com.survey_engine.survey.controller;

import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.survey.service.TemplateService;
import com.survey_engine.survey.dto.TemplateRequest;
import com.survey_engine.survey.dto.TemplateResponse;
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
 * Controller for handling HTTP requests related to Templates.
 */
@RestController
@Validated
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    /**
     * Constructor for TemplateController.
     * @param templateService An instance of TemplateService.
     */
    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Endpoint to create a new template. (Admin only)
     * @param request The request body with template data.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with the created template.
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Valid @RequestBody TemplateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        TemplateResponse response = templateService.createTemplate(request, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint to retrieve all templates.
     * @return A ResponseEntity with a list of all templates.
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        List<TemplateResponse> responses = templateService.getAllTemplates();
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint to retrieve a single template by its ID.
     * @param id The ID of the template.
     * @return A ResponseEntity with the requested template.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Long id) {
        TemplateResponse response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to find templates by survey type.
     * @param type The SurveyType enum.
     * @return A ResponseEntity with a list of matching templates.
     */
    @GetMapping("/filter/type")
    public ResponseEntity<List<TemplateResponse>> getTemplatesByType(@RequestParam SurveyType type) {
        List<TemplateResponse> responses = templateService.getTemplatesByType(type);
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint to find templates by sector.
     * @param sector The industry sector.
     * @return A ResponseEntity with a list of matching templates.
     */
    @GetMapping("/filter/sector")
    public ResponseEntity<List<TemplateResponse>> getTemplatesBySector(@RequestParam String sector) {
        List<TemplateResponse> responses = templateService.getTemplatesBySector(sector);
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint to update an existing template. (Admin only)
     * @param id The ID of the template to update.
     * @param request The request body with updated data.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with the updated template.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        TemplateResponse response = templateService.updateTemplate(id, request, roles);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to delete a template. (Admin only)
     * @param id The ID of the template to delete.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        templateService.deleteTemplate(id, roles);
        return ResponseEntity.noContent().build();
    }
}
