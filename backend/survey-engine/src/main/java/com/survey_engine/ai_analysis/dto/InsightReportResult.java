package com.survey_engine.ai_analysis.dto;

import java.util.List;

/**
 * Structured result returned from AI insight report generation.
 */
public record InsightReportResult(
        String executiveSummary,
        List<KeyFinding> keyFindings,
        List<ActionRecommendation> recommendations,
        List<RespondentCluster> clusters
) {
    public record KeyFinding(
            String type,      // STRENGTH | WEAKNESS | RISK | OPPORTUNITY
            String text,
            String area       // area of the finding e.g. "Customer Service"
    ) {}

    public record ActionRecommendation(
            String priority,          // HIGH | MEDIUM | LOW
            String area,
            String recommendedAction,
            String suggestedOwner,
            String suggestedTimeline
    ) {}

    public record RespondentCluster(
            String name,           // e.g. "High Performers", "At Risk"
            int size,
            String description
    ) {}
}
