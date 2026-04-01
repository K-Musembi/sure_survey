package com.survey_engine.intelligence.repository;

import com.survey_engine.intelligence.domain.InsightReport;
import com.survey_engine.intelligence.domain.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InsightReportRepository extends JpaRepository<InsightReport, UUID> {

    List<InsightReport> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<InsightReport> findBySurveyIdOrderByCreatedAtDesc(Long surveyId);

    List<InsightReport> findByTenantIdAndStatus(Long tenantId, ReportStatus status);

    java.util.Optional<InsightReport> findByIdAndTenantId(UUID id, Long tenantId);
}
