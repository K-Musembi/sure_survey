package com.survey_engine.performance_survey.repository;

import com.survey_engine.performance_survey.models.aggregation.AggregatedScore;
import com.survey_engine.performance_survey.models.aggregation.enums.AggregationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AggregatedScoreRepository extends JpaRepository<AggregatedScore, UUID> {
    Optional<AggregatedScore> findByOrgUnitIdAndPeriodAndStartDate(UUID orgUnitId, AggregationPeriod period, LocalDate startDate);

    List<AggregatedScore> findByOrgUnitIdAndPeriod(UUID orgUnitId, AggregationPeriod period);
}