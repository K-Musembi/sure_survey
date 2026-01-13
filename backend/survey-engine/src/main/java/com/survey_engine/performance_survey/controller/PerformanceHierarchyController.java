package com.survey_engine.performance_survey.controller;

import com.survey_engine.performance_survey.dto.OrgMemberRequest;
import com.survey_engine.performance_survey.dto.OrgMemberResponse;
import com.survey_engine.performance_survey.dto.OrgUnitRequest;
import com.survey_engine.performance_survey.dto.OrgUnitResponse;
import com.survey_engine.performance_survey.service.HierarchyService;
import com.survey_engine.user.UserApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/performance/hierarchy")
@RequiredArgsConstructor
public class PerformanceHierarchyController {

    private final HierarchyService hierarchyService;
    private final UserApi userApi;

    @PostMapping("/nodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrgUnitResponse> createOrgUnit(@Valid @RequestBody OrgUnitRequest request) {
        return ResponseEntity.ok(hierarchyService.createOrgUnit(request));
    }

    @PostMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrgMemberResponse> addMember(@Valid @RequestBody OrgMemberRequest request) {
        return ResponseEntity.ok(hierarchyService.addMember(request));
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<OrgUnitResponse>> getHierarchy() {
        return ResponseEntity.ok(hierarchyService.getHierarchy(userApi.getTenantId()));
    }
}