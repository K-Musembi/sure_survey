package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.scoring.SurveyScoringSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyScoringSchemaRepository extends JpaRepository<SurveyScoringSchema, UUID> {

    Optional<SurveyScoringSchema> findBySurveyId(Long surveyId);
}