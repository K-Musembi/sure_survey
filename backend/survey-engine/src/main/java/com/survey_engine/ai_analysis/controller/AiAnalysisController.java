package com.survey_engine.ai_analysis.controller;

import com.survey_engine.ai_analysis.dto.AiGeneratedQuestion;
import com.survey_engine.ai_analysis.dto.AiResponse;
import com.survey_engine.ai_analysis.dto.SurveyAnalysisRequest;
import com.survey_engine.ai_analysis.dto.SurveyGenerationRequest;
import com.survey_engine.ai_analysis.service.AiAnalysisAgent;
import com.survey_engine.ai_analysis.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the AI Analysis module.
 * Exposes endpoints for interacting with the AI services:
 * {@code /generate}: Generates survey questions based on a topic and sector.
 * {@code /analyze}: Performs deep analysis on collected survey responses using an AI Agent.
 * These endpoints delegate logic to {@link AiGenerationService} and {@link AiAnalysisAgent} respectively.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiGenerationService generationService;
    private final AiAnalysisAgent analysisAgent;

    /**
     * Generates a list of survey questions.
     *
     * @param request The generation criteria.
     * @return A list of generated questions.
     */
    @PostMapping("/generate")
    public ResponseEntity<List<AiGeneratedQuestion>> generateSurvey(@RequestBody SurveyGenerationRequest request) {
        return ResponseEntity.ok(generationService.generateSurveyQuestions(request));
    }

    /**
     * Analyzes responses for a given survey.
     *
     * @param request The analysis request containing the survey ID and query.
     * @return The text result of the analysis.
     */
    @PostMapping("/analyze")
    public ResponseEntity<AiResponse> analyzeSurvey(@RequestBody SurveyAnalysisRequest request) {
        return ResponseEntity.ok(analysisAgent.analyzeSurvey(request));
    }
}