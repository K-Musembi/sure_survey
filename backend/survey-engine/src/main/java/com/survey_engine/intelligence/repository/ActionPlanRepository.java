package com.survey_engine.intelligence.repository;

import com.survey_engine.intelligence.domain.ActionPlan;
import com.survey_engine.intelligence.domain.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionPlanRepository extends JpaRepository<ActionPlan, UUID> {

    List<ActionPlan> findByReportIdOrderByPriorityAsc(UUID reportId);

    List<ActionPlan> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<ActionPlan> findByTenantIdAndStatus(Long tenantId, PlanStatus status);

    long countByTenantIdAndStatus(Long tenantId, PlanStatus status);
}
