package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.structure.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {
    List<OrgUnit> findByParentId(UUID parentId);

    List<OrgUnit> findByTenantId(Long tenantId);
}