package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.scoring.QuestionScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionScoringRuleRepository extends JpaRepository<QuestionScoringRule, UUID> {

    List<QuestionScoringRule> findBySchemaId(UUID schemaId);
}