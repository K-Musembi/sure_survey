package com.survey_engine.performance_survey.controller;

import com.survey_engine.performance_survey.dto.SurveyScoringSchemaRequest;
import com.survey_engine.performance_survey.dto.SurveyScoringSchemaResponse;
import com.survey_engine.performance_survey.service.ScoringConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performance/scoring-schema")
@RequiredArgsConstructor
public class PerformanceConfigurationController {

    private final ScoringConfigurationService configService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyScoringSchemaResponse> configureScoring(@Valid @RequestBody SurveyScoringSchemaRequest request) {
        return ResponseEntity.ok(configService.createOrUpdateSchema(request));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyScoringSchemaResponse> getSchema(@PathVariable Long surveyId) {
        return ResponseEntity.ok(configService.getSchema(surveyId));
    }
}