package com.survey_engine.ai_analysis;

import com.survey_engine.ai_analysis.dto.AiGeneratedQuestion;
import com.survey_engine.ai_analysis.dto.AiResponse;
import com.survey_engine.ai_analysis.dto.InsightReportRequest;
import com.survey_engine.ai_analysis.dto.InsightReportResult;
import com.survey_engine.ai_analysis.dto.SurveyAnalysisRequest;
import com.survey_engine.ai_analysis.dto.SurveyGenerationRequest;
import com.survey_engine.ai_analysis.service.AiAnalysisAgent;
import com.survey_engine.ai_analysis.service.AiGenerationService;
import com.survey_engine.ai_analysis.service.AiInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the public AI module API.
 * Delegates to internal services — callers outside this module see only AiApi.
 */
@Service
@RequiredArgsConstructor
public class AiApiImpl implements AiApi {

    private final AiGenerationService generationService;
    private final AiAnalysisAgent analysisAgent;
    private final AiInsightService insightService;

    @Override
    public List<AiGeneratedQuestion> generateSurveyQuestions(SurveyGenerationRequest request) {
        return generationService.generateSurveyQuestions(request);
    }

    @Override
    public String analyzeSurveyResponses(Long surveyId, String contextPrompt) {
        AiResponse response = analysisAgent.analyzeSurvey(new SurveyAnalysisRequest(surveyId, contextPrompt));
        return response.result();
    }

    @Override
    public InsightReportResult generateInsightReport(InsightReportRequest request) {
        return insightService.generateInsightReport(request);
    }

    @Override
    public String analyzeSentiment(String text) {
        return insightService.analyzeSentiment(text);
    }
}
