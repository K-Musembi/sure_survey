package com.survey_engine.intelligence.dto;

import com.survey_engine.intelligence.domain.InsightReport;

import java.util.List;

public record ReportSummary(
        long pendingActions,
        long inProgressActions,
        long completedActions,
        List<InsightReport> recentReports
) {}
