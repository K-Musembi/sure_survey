package com.survey_engine.ai_analysis;

import com.survey_engine.ai_analysis.dto.AiGeneratedQuestion;
import com.survey_engine.ai_analysis.dto.InsightReportRequest;
import com.survey_engine.ai_analysis.dto.InsightReportResult;
import com.survey_engine.ai_analysis.dto.SurveyGenerationRequest;
import org.springframework.modulith.NamedInterface;

import java.util.List;

/**
 * Public API for the ai_analysis module.
 * All cross-module AI operations go through this interface.
 * Implementations must not be referenced directly from other modules.
 */
@NamedInterface("ai")
public interface AiApi {

    /**
     * Generate survey questions for a given topic and sector.
     */
    List<AiGeneratedQuestion> generateSurveyQuestions(SurveyGenerationRequest request);

    /**
     * Analyze an existing survey's responses and return a natural-language summary.
     */
    String analyzeSurveyResponses(Long surveyId, String contextPrompt);

    /**
     * Generate a structured insight report from pre-assembled survey data.
     * Called by the intelligence module after it has fetched response data.
     */
    InsightReportResult generateInsightReport(InsightReportRequest request);

    /**
     * Analyze the sentiment of a single open-text response.
     * Returns one of: POSITIVE | NEUTRAL | NEGATIVE
     */
    String analyzeSentiment(String text);

    /**
     * Suggests branch rules for a survey based on its questions and answer options.
     * Returns a JSON string containing an array of suggested branch rules.
     */
    String suggestBranchRules(String surveyContext);
}
