package com.survey_engine.survey.controller;

import com.survey_engine.survey.dto.BranchRuleRequest;
import com.survey_engine.survey.dto.BranchRuleResponse;
import com.survey_engine.survey.dto.MilestoneRequest;
import com.survey_engine.survey.models.SurveyMilestone;
import com.survey_engine.survey.service.BranchRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Manages branching logic and completion milestones for a survey.
 * All endpoints are scoped under /api/v1/surveys/{surveyId}/...
 */
@RestController
@RequestMapping("/api/v1/surveys/{surveyId}")
@RequiredArgsConstructor
public class BranchRuleController {

    private final BranchRuleService branchRuleService;

    @PostMapping("/branch-rules")
    public ResponseEntity<BranchRuleResponse> createRule(
            @PathVariable Long surveyId,
            @Valid @RequestBody BranchRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchRuleService.createRule(surveyId, request));
    }

    @GetMapping("/branch-rules")
    public ResponseEntity<List<BranchRuleResponse>> getRules(@PathVariable Long surveyId) {
        return ResponseEntity.ok(branchRuleService.getRulesForSurvey(surveyId));
    }

    @DeleteMapping("/branch-rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long surveyId,
            @PathVariable Long ruleId) {
        branchRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/milestones")
    public ResponseEntity<SurveyMilestone> createMilestone(
            @PathVariable Long surveyId,
            @Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchRuleService.createMilestone(surveyId, request));
    }

    @GetMapping("/milestones")
    public ResponseEntity<List<SurveyMilestone>> getMilestones(@PathVariable Long surveyId) {
        return ResponseEntity.ok(branchRuleService.getMilestonesForSurvey(surveyId));
    }
}
