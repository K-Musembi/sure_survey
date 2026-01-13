package com.survey_engine.performance_survey.service;

import com.survey_engine.performance_survey.dto.BadgeResponse;
import com.survey_engine.performance_survey.dto.GamificationProfileResponse;
import com.survey_engine.performance_survey.models.gamification.GamificationProfile;
import com.survey_engine.performance_survey.models.gamification.UserBadge;
import com.survey_engine.performance_survey.repository.BadgeDefinitionRepository;
import com.survey_engine.performance_survey.repository.GamificationProfileRepository;
import com.survey_engine.performance_survey.repository.UserBadgeRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationProfileRepository profileRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserApi userApi;

    @Transactional(readOnly = true)
    public GamificationProfileResponse getProfile(String userId) {
        GamificationProfile profile = profileRepository.findByUserId(userId)
                .orElse(new GamificationProfile(null, userId, 0L, 0, 1)); // Return empty/default if not found

        List<UserBadge> badges = userBadgeRepository.findByUserId(userId);
        
        List<BadgeResponse> badgeResponses = badges.stream()
                .map(ub -> new BadgeResponse(
                        ub.getBadge().getId(),
                        ub.getBadge().getName(),
                        ub.getBadge().getIconUrl()
                ))
                .collect(Collectors.toList());

        return new GamificationProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getTotalPoints(),
                profile.getCurrentStreak(),
                profile.getLevel(),
                badgeResponses
        );
    }
    
    // Future: Method to award points called by ExecutionService
    @Transactional
    public void awardPoints(String userId, Long points) {
        GamificationProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    GamificationProfile p = new GamificationProfile();
                    p.setUserId(userId);
                    p.setTenantId(userApi.getTenantId()); // Best effort tenant guess
                    return p;
                });
        
        profile.setTotalPoints(profile.getTotalPoints() + points);
        // Simple level logic: 1 level per 1000 points
        profile.setLevel((int) (profile.getTotalPoints() / 1000) + 1);
        
        profileRepository.save(profile);
    }
}