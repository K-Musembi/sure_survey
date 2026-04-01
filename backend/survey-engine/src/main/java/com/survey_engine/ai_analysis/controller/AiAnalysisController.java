package com.survey_engine.ai_analysis.controller;

import com.survey_engine.ai_analysis.dto.AiGeneratedQuestion;
import com.survey_engine.ai_analysis.dto.AiResponse;
import com.survey_engine.ai_analysis.dto.SurveyAnalysisRequest;
import com.survey_engine.ai_analysis.dto.SurveyGenerationRequest;
import com.survey_engine.ai_analysis.service.AiAnalysisAgent;
import com.survey_engine.ai_analysis.service.AiGenerationService;
import com.survey_engine.ai_analysis.service.AiInsightService;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.survey.SurveyApi;
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
    private final AiInsightService insightService;
    private final SurveyApi surveyApi;

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

    /**
     * Uses AI to suggest branch rules for a survey based on its question structure.
     * The user can review the suggestions and selectively apply them via
     * POST /api/v1/surveys/{surveyId}/branch-rules.
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/surveys/{surveyId}/branch-rules/suggest")
    public ResponseEntity<String> suggestBranchRules(@PathVariable Long surveyId) {
        java.util.Map<String, Object> surveyData = surveyApi.getSurveyById(surveyId);
        if (surveyData == null) {
            throw new ResourceNotFoundException("SURVEY_NOT_FOUND", "Survey not found: " + surveyId);
        }

        java.util.List<java.util.Map<String, Object>> questions =
                (java.util.List<java.util.Map<String, Object>>) surveyData.get("questions");
        if (questions == null || questions.isEmpty()) {
            throw new BusinessRuleException("NO_QUESTIONS",
                    "Survey has no questions to suggest branching rules for.");
        }

        StringBuilder context = new StringBuilder();
        context.append("Survey: ").append(surveyData.get("name")).append("\n");
        context.append("Questions:\n");
        for (java.util.Map<String, Object> q : questions) {
            context.append("  ID=").append(q.get("id"))
                    .append(" Type=").append(q.get("questionType"))
                    .append(" Text=\"").append(q.get("questionText")).append("\"");
            if (q.get("options") != null) {
                context.append(" Options=").append(q.get("options"));
            }
            if (q.get("category") != null) {
                context.append(" Category=").append(q.get("category"));
            }
            if (q.get("weight") != null) {
                context.append(" Weight=").append(q.get("weight"));
            }
            context.append("\n");
        }

        return ResponseEntity.ok(insightService.suggestBranchRules(context.toString()));
    }
}