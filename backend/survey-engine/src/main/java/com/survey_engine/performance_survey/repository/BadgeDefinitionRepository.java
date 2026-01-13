package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.gamification.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, UUID> {
}