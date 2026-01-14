package com.survey_engine.performance_survey.service;

import com.survey_engine.performance_survey.dto.PerformanceSubjectRequest;
import com.survey_engine.performance_survey.dto.PerformanceSubjectResponse;
import com.survey_engine.performance_survey.dto.OrgUnitRequest;
import com.survey_engine.performance_survey.dto.OrgUnitResponse;
import com.survey_engine.performance_survey.models.structure.PerformanceSubject;
import com.survey_engine.performance_survey.models.structure.OrgUnit;
import com.survey_engine.performance_survey.repository.PerformanceSubjectRepository;
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
    private final PerformanceSubjectRepository performanceSubjectRepository;
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
    public PerformanceSubjectResponse addSubject(PerformanceSubjectRequest request) {
        OrgUnit orgUnit = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new EntityNotFoundException("OrgUnit not found"));

        PerformanceSubject subject = new PerformanceSubject();
        subject.setUserId(request.userId());
        subject.setReferenceCode(request.referenceCode());
        subject.setDisplayName(request.displayName());
        subject.setType(request.type());
        subject.setOrgUnit(orgUnit);
        subject.setRole(request.role());
        subject.setTenantId(userApi.getTenantId());

        PerformanceSubject saved = performanceSubjectRepository.save(subject);
        return mapToSubjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> getHierarchy(Long tenantId) {
        return orgUnitRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrgUnit getOrgUnitForSubject(String referenceCode) {
        return performanceSubjectRepository.findByReferenceCodeAndTenantId(referenceCode, userApi.getTenantId())
                .map(PerformanceSubject::getOrgUnit)
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

    private PerformanceSubjectResponse mapToSubjectResponse(PerformanceSubject subject) {
        return new PerformanceSubjectResponse(
                subject.getId(),
                subject.getUserId(),
                subject.getReferenceCode(),
                subject.getDisplayName(),
                subject.getType(),
                subject.getOrgUnit().getId(),
                subject.getOrgUnit().getName(),
                subject.getRole()
        );
    }
}
