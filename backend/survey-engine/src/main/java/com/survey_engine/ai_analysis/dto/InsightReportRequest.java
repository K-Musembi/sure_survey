package com.survey_engine.ai_analysis.dto;

import java.util.List;

/**
 * Input for the AI insight report generation.
 * The caller (intelligence module) assembles the raw data;
 * the AI module produces the structured analysis.
 */
public record InsightReportRequest(
        Long surveyId,
        String surveyName,
        String sector,
        int totalResponses,
        List<QuestionSummary> questionSummaries,
        List<String> openTextResponses
) {
    public record QuestionSummary(
            String questionText,
            String questionType,
            String category,
            String optionBreakdown  // JSON: {"Option A": 12, "Option B": 8}
    ) {}
}
