package com.survey_engine.intelligence.service;

import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.intelligence.domain.ActionPlan;
import com.survey_engine.intelligence.domain.enums.PlanStatus;
import com.survey_engine.intelligence.dto.UpdateActionPlanRequest;
import com.survey_engine.intelligence.repository.ActionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActionPlanService {

    private final ActionPlanRepository actionPlanRepository;

    public List<ActionPlan> getPlansForReport(UUID reportId) {
        return actionPlanRepository.findByReportIdOrderByPriorityAsc(reportId);
    }

    public List<ActionPlan> getPlansForTenant(Long tenantId) {
        return actionPlanRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public List<ActionPlan> getPlansForTenantByStatus(Long tenantId, PlanStatus status) {
        return actionPlanRepository.findByTenantIdAndStatus(tenantId, status);
    }

    @Transactional
    public ActionPlan updatePlan(UUID planId, UpdateActionPlanRequest request) {
        ActionPlan plan = actionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("INTELLIGENCE_ACTION_PLAN_NOT_FOUND",
                        "Action plan not found: " + planId));

        if (plan.getStatus() == PlanStatus.DISMISSED && request.status() != PlanStatus.PENDING) {
            throw new BusinessRuleException("INTELLIGENCE_INVALID_PLAN_TRANSITION",
                    "Cannot change status of a dismissed action plan.");
        }

        if (request.status() != null) {
            plan.setStatus(request.status());
        }
        if (request.completionNotes() != null) {
            plan.setCompletionNotes(request.completionNotes());
        }
        if (request.dueDate() != null) {
            plan.setDueDate(request.dueDate());
        }

        return actionPlanRepository.save(plan);
    }
}
