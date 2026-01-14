package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.structure.PerformanceSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceSubjectRepository extends JpaRepository<PerformanceSubject, UUID> {
    Optional<PerformanceSubject> findByUserId(String userId);

    Optional<PerformanceSubject> findByReferenceCodeAndTenantId(String referenceCode, Long tenantId);

    List<PerformanceSubject> findByOrgUnitId(UUID orgUnitId);
}
