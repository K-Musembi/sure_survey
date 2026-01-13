package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.gamification.GamificationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, UUID> {
    Optional<GamificationProfile> findByUserId(String userId);
}