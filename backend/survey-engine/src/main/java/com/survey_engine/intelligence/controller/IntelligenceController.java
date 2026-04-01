package com.survey_engine.intelligence.controller;

import com.survey_engine.intelligence.domain.ActionPlan;
import com.survey_engine.intelligence.domain.enums.PlanStatus;
import com.survey_engine.intelligence.domain.InsightReport;
import com.survey_engine.intelligence.dto.GenerateReportRequest;
import com.survey_engine.intelligence.dto.ReportSummary;
import com.survey_engine.intelligence.dto.UpdateActionPlanRequest;
import com.survey_engine.intelligence.service.ActionPlanService;
import com.survey_engine.intelligence.service.InsightGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Decision Intelligence Platform endpoints.
 * Reports are generated asynchronously — POST to request, GET to poll status.
 */
@RestController
@RequestMapping("/api/v1/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final InsightGenerationService insightService;
    private final ActionPlanService actionPlanService;

    @PostMapping("/reports")
    public ResponseEntity<Map<String, Object>> requestReport(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GenerateReportRequest request) {
        Long tenantId = extractTenantId(jwt);
        UUID reportId = insightService.requestReport(tenantId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("reportId", reportId, "status", "GENERATING"));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<InsightReport> getReport(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reportId) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(insightService.getReport(reportId, tenantId));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<InsightReport>> getReportsForTenant(@AuthenticationPrincipal Jwt jwt) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(insightService.getReportsForTenant(tenantId));
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getSummary(@AuthenticationPrincipal Jwt jwt) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(insightService.getSummary(tenantId));
    }

    @GetMapping("/reports/{reportId}/action-plans")
    public ResponseEntity<List<ActionPlan>> getPlansForReport(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reportId) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(actionPlanService.getPlansForReport(reportId, tenantId));
    }

    @GetMapping("/action-plans")
    public ResponseEntity<List<ActionPlan>> getPlansForTenant(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) PlanStatus status) {
        Long tenantId = extractTenantId(jwt);
        if (status != null) {
            return ResponseEntity.ok(actionPlanService.getPlansForTenantByStatus(tenantId, status));
        }
        return ResponseEntity.ok(actionPlanService.getPlansForTenant(tenantId));
    }

    @PatchMapping("/action-plans/{planId}")
    public ResponseEntity<ActionPlan> updatePlan(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID planId,
            @RequestBody UpdateActionPlanRequest request) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(actionPlanService.updatePlan(planId, tenantId, request));
    }

    private Long extractTenantId(Jwt jwt) {
        return jwt.getClaim("tenantId");
    }
}
