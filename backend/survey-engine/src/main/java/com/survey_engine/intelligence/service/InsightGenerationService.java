package com.survey_engine.intelligence.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.ai_analysis.AiApi;
import com.survey_engine.ai_analysis.dto.InsightReportRequest;
import com.survey_engine.ai_analysis.dto.InsightReportResult;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.intelligence.domain.ActionPlan;
import com.survey_engine.intelligence.domain.InsightReport;
import com.survey_engine.intelligence.domain.enums.PlanStatus;
import com.survey_engine.intelligence.domain.enums.Priority;
import com.survey_engine.intelligence.domain.enums.ReportStatus;
import com.survey_engine.intelligence.domain.enums.ReportType;
import com.survey_engine.intelligence.dto.GenerateReportRequest;
import com.survey_engine.intelligence.dto.ReportSummary;
import com.survey_engine.intelligence.repository.ActionPlanRepository;
import com.survey_engine.intelligence.repository.InsightReportRepository;
import com.survey_engine.survey.SurveyApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightGenerationService {

    private final InsightReportRepository reportRepository;
    private final ActionPlanRepository actionPlanRepository;
    private final AiApi aiApi;
    private final SurveyApi surveyApi;
    private final ObjectMapper objectMapper;

    /**
     * Create a report record and kick off async generation.
     * Returns immediately with the report ID; the caller polls for status.
     */
    @Transactional
    public UUID requestReport(Long tenantId, GenerateReportRequest request) {
        InsightReport report = new InsightReport();
        report.setTenantId(tenantId);
        report.setSurveyId(request.surveyId());
        report.setReportType(ReportType.SINGLE_SURVEY);
        report.setTitle(request.title() != null ? request.title()
                : "Survey Analysis Report");
        report.setPeriodStart(request.periodStart());
        report.setPeriodEnd(request.periodEnd());
        report.setStatus(ReportStatus.PENDING);

        InsightReport saved = reportRepository.save(report);
        generateAsync(saved.getId(), request.sector());
        return saved.getId();
    }

    @Async
    public void generateAsync(UUID reportId, String sector) {
        InsightReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("INTELLIGENCE_REPORT_NOT_FOUND",
                        "Report not found: " + reportId));

        report.setStatus(ReportStatus.GENERATING);
        reportRepository.save(report);

        try {
            // Fetch response data via SurveyApi
            Map<String, Object> surveyData = surveyApi.getSurveyById(report.getSurveyId());
            if (surveyData == null || surveyData.isEmpty()) {
                throw new ResourceNotFoundException("INTELLIGENCE_SURVEY_DATA_NOT_FOUND",
                        "No data found for survey " + report.getSurveyId());
            }

            String surveyName = (String) surveyData.getOrDefault("name", "Survey");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawResponses =
                    (List<Map<String, Object>>) surveyData.getOrDefault("responses", List.of());

            // Build the AI request
            InsightReportRequest aiRequest = buildAiRequest(
                    report.getSurveyId(), surveyName, sector,
                    rawResponses, surveyData);

            // Call AI
            InsightReportResult result = aiApi.generateInsightReport(aiRequest);

            // Persist results
            report.setExecutiveSummary(result.executiveSummary());
            report.setKeyFindings(objectMapper.writeValueAsString(result.keyFindings()));
            report.setRecommendations(objectMapper.writeValueAsString(result.recommendations()));
            report.setRespondentClusters(objectMapper.writeValueAsString(result.clusters()));
            report.setResponseCount(rawResponses.size());
            report.setStatus(ReportStatus.READY);
            report.setGeneratedAt(LocalDateTime.now());
            reportRepository.save(report);

            // Generate action plans from recommendations
            createActionPlans(report, result.recommendations());

            log.info("Insight report {} generated successfully for survey {}",
                    reportId, report.getSurveyId());

        } catch (Exception e) {
            log.error("Insight report generation failed for report {}: {}", reportId, e.getMessage(), e);
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            reportRepository.save(report);
        }
    }

    private InsightReportRequest buildAiRequest(Long surveyId, String surveyName, String sector,
                                                List<Map<String, Object>> rawResponses,
                                                Map<String, Object> surveyData) {
        // Build question summaries from survey data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions =
                (List<Map<String, Object>>) surveyData.getOrDefault("questions", List.of());

        List<InsightReportRequest.QuestionSummary> summaries = new ArrayList<>();
        List<String> openTextResponses = new ArrayList<>();

        for (Map<String, Object> q : questions) {
            String qText = (String) q.getOrDefault("questionText", "");
            String qType = (String) q.getOrDefault("questionType", "");
            String category = (String) q.get("category");

            summaries.add(new InsightReportRequest.QuestionSummary(
                    qText, qType, category, buildOptionBreakdown(q, rawResponses)));

            if ("FREE_TEXT".equals(qType)) {
                extractOpenTextResponses(q, rawResponses, openTextResponses);
            }
        }

        return new InsightReportRequest(surveyId, surveyName,
                sector != null ? sector : "General",
                rawResponses.size(), summaries, openTextResponses);
    }

    private String buildOptionBreakdown(Map<String, Object> question,
                                        List<Map<String, Object>> responses) {
        // Simplified placeholder — real impl counts per-option answers from responses
        return "Response data for analysis";
    }

    private void extractOpenTextResponses(Map<String, Object> question,
                                          List<Map<String, Object>> responses,
                                          List<String> accumulator) {
        // Extract open-text answers from response data
    }

    @Transactional
    private void createActionPlans(InsightReport report,
                                   List<InsightReportResult.ActionRecommendation> recommendations) {
        if (recommendations == null) return;

        List<ActionPlan> plans = new ArrayList<>();
        for (InsightReportResult.ActionRecommendation rec : recommendations) {
            ActionPlan plan = new ActionPlan();
            plan.setReportId(report.getId());
            plan.setTenantId(report.getTenantId());
            plan.setPriority(parsePriority(rec.priority()));
            plan.setArea(rec.area());
            plan.setRecommendedAction(rec.recommendedAction());
            plan.setSuggestedOwner(rec.suggestedOwner());
            plan.setSuggestedTimeline(rec.suggestedTimeline());
            plan.setStatus(PlanStatus.PENDING);
            plans.add(plan);
        }
        actionPlanRepository.saveAll(plans);
        log.debug("Created {} action plans for report {}", plans.size(), report.getId());
    }

    private Priority parsePriority(String priority) {
        try {
            return Priority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            return Priority.MEDIUM;
        }
    }

    public InsightReport getReport(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("INTELLIGENCE_REPORT_NOT_FOUND",
                        "Report not found: " + reportId));
    }

    public InsightReport getReport(UUID reportId, Long tenantId) {
        return reportRepository.findByIdAndTenantId(reportId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("INTELLIGENCE_REPORT_NOT_FOUND",
                        "Report not found: " + reportId));
    }

    public List<InsightReport> getReportsForTenant(Long tenantId) {
        return reportRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public ReportSummary getSummary(Long tenantId) {
        long pending = actionPlanRepository.countByTenantIdAndStatus(tenantId, PlanStatus.PENDING);
        long inProgress = actionPlanRepository.countByTenantIdAndStatus(tenantId, PlanStatus.IN_PROGRESS);
        long completed = actionPlanRepository.countByTenantIdAndStatus(tenantId, PlanStatus.COMPLETED);
        List<InsightReport> recent = reportRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().limit(5).toList();
        return new ReportSummary(pending, inProgress, completed, recent);
    }
}
