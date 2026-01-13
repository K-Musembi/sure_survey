package com.survey_engine.performance_survey.service;

import com.survey_engine.performance_survey.events.ScoreCalculatedEvent;
import com.survey_engine.performance_survey.models.aggregation.AggregatedScore;
import com.survey_engine.performance_survey.models.aggregation.enums.AggregationPeriod;
import com.survey_engine.performance_survey.models.structure.OrgUnit;
import com.survey_engine.performance_survey.repository.AggregatedScoreRepository;
import com.survey_engine.performance_survey.repository.OrgUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final AggregatedScoreRepository aggregatedScoreRepository;
    private final OrgUnitRepository orgUnitRepository;

    @Async
    @EventListener
    @Transactional
    public void handleScoreCalculated(ScoreCalculatedEvent event) {
        if (event.orgUnitId() == null) return;

        log.info("Aggregating score for OrgUnit: {}", event.orgUnitId());
        
        // Recursive aggregation logic would go here.
        // For MVP, we just update the immediate parent unit's daily/weekly average.
        
        updateAggregate(event.orgUnitId(), event.normalizedScore());
        
        // Walk up the tree
        OrgUnit unit = orgUnitRepository.findById(event.orgUnitId()).orElse(null);
        while (unit != null && unit.getParentId() != null) {
            // Simplified: Just propagating the same score to verify flow. 
            // Real logic needs to fetch all children scores and re-calculate.
            // That is heavy, so typically done via scheduled jobs or incrementally.
            unit = orgUnitRepository.findById(unit.getParentId()).orElse(null);
        }
    }

    private void updateAggregate(UUID orgUnitId, Double newScore) {
        OrgUnit unit = orgUnitRepository.findById(orgUnitId).orElseThrow();
        LocalDate today = LocalDate.now();
        
        AggregatedScore agg = aggregatedScoreRepository.findByOrgUnitIdAndPeriodAndStartDate(
                orgUnitId, AggregationPeriod.WEEKLY, today) // Simplified assumption: StartDate = Today
                .orElse(new AggregatedScore(null, unit, AggregationPeriod.WEEKLY, today, 0.0, 0));
        
        // Incremental Average Calculation
        double currentTotal = agg.getScore() * agg.getSampleSize();
        double newTotal = currentTotal + newScore;
        int newSize = agg.getSampleSize() + 1;
        
        agg.setScore(newTotal / newSize);
        agg.setSampleSize(newSize);
        
        aggregatedScoreRepository.save(agg);
    }
}