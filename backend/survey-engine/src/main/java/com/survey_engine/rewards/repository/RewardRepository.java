package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {
    Optional<Reward> findBySurveyId(String surveyId);

    List<Reward> findByUserId(String userId);
}