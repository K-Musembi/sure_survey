package com.survey_engine.performance_survey.controller;

import com.survey_engine.performance_survey.dto.GamificationProfileResponse;
import com.survey_engine.performance_survey.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performance/dashboards")
@RequiredArgsConstructor
public class PerformanceDashboardController {

    private final GamificationService gamificationService;

    @GetMapping("/my-profile")
    public ResponseEntity<GamificationProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(gamificationService.getProfile(userId));
    }
}