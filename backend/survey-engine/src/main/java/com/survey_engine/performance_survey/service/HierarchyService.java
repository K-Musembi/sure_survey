package com.survey_engine.performance_survey.service;

import com.survey_engine.performance_survey.dto.OrgMemberRequest;
import com.survey_engine.performance_survey.dto.OrgMemberResponse;
import com.survey_engine.performance_survey.dto.OrgUnitRequest;
import com.survey_engine.performance_survey.dto.OrgUnitResponse;
import com.survey_engine.performance_survey.models.structure.OrgMember;
import com.survey_engine.performance_survey.models.structure.OrgUnit;
import com.survey_engine.performance_survey.repository.OrgMemberRepository;
import com.survey_engine.performance_survey.repository.OrgUnitRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyService {

    private final OrgUnitRepository orgUnitRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final UserApi userApi;

    @Transactional
    public OrgUnitResponse createOrgUnit(OrgUnitRequest request) {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setName(request.name());
        orgUnit.setType(request.type());
        orgUnit.setTenantId(userApi.getTenantId());
        
        if (request.parentId() != null) {
            if (!orgUnitRepository.existsById(request.parentId())) {
                throw new EntityNotFoundException("Parent OrgUnit not found");
            }
            orgUnit.setParentId(request.parentId());
        }
        
        orgUnit.setManagerId(request.managerId());
        
        OrgUnit saved = orgUnitRepository.save(orgUnit);
        return mapToResponse(saved);
    }

    @Transactional
    public OrgMemberResponse addMember(OrgMemberRequest request) {
        OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new EntityNotFoundException("OrgUnit not found"));

        // Check if user exists via UserApi
        // userApi.findUserById(request.userId()) .orElseThrow(...) 
        // For now, assuming user exists or let FK constraint fail if we had one (soft link here).

        OrgMember member = new OrgMember();
        member.setUserId(request.userId());
        member.setOrgUnit(orgUnit);
        member.setRole(request.role());
        member.setTenantId(userApi.getTenantId());

        OrgMember saved = orgMemberRepository.save(member);
        return mapToMemberResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> getHierarchy(Long tenantId) {
        // Simple list for now, frontend can reconstruct tree
        return orgUnitRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrgUnit getOrgUnitForUser(String userId) {
        return orgMemberRepository.findByUserId(userId)
                .map(OrgMember::getOrgUnit)
                .orElse(null);
    }

    private OrgUnitResponse mapToResponse(OrgUnit unit) {
        return new OrgUnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getType(),
                unit.getParentId(),
                unit.getManagerId()
        );
    }

    private OrgMemberResponse mapToMemberResponse(OrgMember member) {
        return new OrgMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getOrgUnit().getId(),
                member.getOrgUnit().getName(),
                member.getRole()
        );
    }
}