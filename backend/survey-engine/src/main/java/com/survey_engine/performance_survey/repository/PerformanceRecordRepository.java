package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.scoring.PerformanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, UUID> {
    Optional<PerformanceRecord> findByResponseId(Long responseId);

    List<PerformanceRecord> findBySubject_UserId(String userId);

    List<PerformanceRecord> findBySubject_ReferenceCode(String referenceCode);

    List<PerformanceRecord> findByOrgUnitId(UUID orgUnitId);
}
