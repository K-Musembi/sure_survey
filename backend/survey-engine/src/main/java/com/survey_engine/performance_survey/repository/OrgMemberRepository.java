package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.structure.OrgMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgMemberRepository extends JpaRepository<OrgMember, UUID> {
    Optional<OrgMember> findByUserId(String userId);

    List<OrgMember> findByOrgUnitId(UUID orgUnitId);
}