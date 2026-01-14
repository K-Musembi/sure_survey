package com.survey_engine.performance_survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.performance_survey.events.ScoreCalculatedEvent;
import com.survey_engine.performance_survey.models.scoring.PerformanceRecord;
import com.survey_engine.performance_survey.models.scoring.QuestionScoringRule;
import com.survey_engine.performance_survey.models.scoring.SurveyScoringSchema;
import com.survey_engine.performance_survey.models.scoring.enums.ScoringStrategy;
import com.survey_engine.performance_survey.models.structure.PerformanceSubject;
import com.survey_engine.performance_survey.repository.PerformanceRecordRepository;
import com.survey_engine.performance_survey.repository.PerformanceSubjectRepository;
import com.survey_engine.survey.SurveyApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceExecutionService {

    private final ScoringConfigurationService configService;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final PerformanceSubjectRepository performanceSubjectRepository;
    private final SurveyApi surveyApi;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handleSurveyCompletion(SurveyCompletedEvent event) {
        log.info("Checking performance eligibility for survey {}", event.surveyId());

        // 1. Check if scoring schema exists
        SurveyScoringSchema schema = configService.findSchemaEntity(event.surveyId());
        if (schema == null) {
            log.info("No scoring schema found for survey {}. Ignoring.", event.surveyId());
            return;
        }

        // 2. Fetch answers
        var responseOpt = surveyApi.getResponseRepository().findById(event.responseId());
        if (responseOpt.isEmpty()) return;
        var response = responseOpt.get();

        // 3. Calculate Score
        List<QuestionScoringRule> rules = configService.findRulesEntity(schema.getId());
        Map<Long, QuestionScoringRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(QuestionScoringRule::getQuestionId, r -> r));

        double totalScore = 0.0;

        for (var answer : response.getAnswers()) {
            QuestionScoringRule rule = ruleMap.get(answer.getQuestion().getId());
            if (rule != null) {
                double questionScore = calculateQuestionScore(rule, answer.getAnswerValue());
                totalScore += questionScore;
            }
        }

        // 4. Normalize
        double normalized = 0.0;
        if (schema.getTargetScore() != null && schema.getTargetScore() > 0) {
            normalized = (totalScore / schema.getTargetScore()) * 100.0;
        }

        // 5. Determine Subject
        PerformanceSubject subject = resolveSubject(event.responseId(), schema.getTenantId());
        if (subject == null) {
            log.warn("Could not identify performance subject for response {}. Skipping record.", event.responseId());
            return;
        }

        // 6. Save Record
        PerformanceRecord record = new PerformanceRecord();
        record.setSurveyId(event.surveyId());
        record.setResponseId(event.responseId());
        record.setSubject(subject);
        record.setEvaluatorUserId(event.responderId());
        record.setRawScore(totalScore);
        record.setNormalizedScore(normalized);
        record.setOrgUnitId(subject.getOrgUnit().getId());
        record.setRecordedAt(LocalDateTime.now());
        record.setTenantId(schema.getTenantId());

        performanceRecordRepository.save(record);
        log.info("Saved performance record for subject {}: Score {}", subject.getReferenceCode(), normalized);
        
        // Publish event for Aggregation and Gamification
        eventPublisher.publishEvent(new ScoreCalculatedEvent(
                record.getId(),
                subject.getId(),
                subject.getUserId(),
                record.getNormalizedScore(),
                record.getOrgUnitId()
        ));
    }

    private PerformanceSubject resolveSubject(Long responseId, Long tenantId) {
        var response = surveyApi.getResponseRepository().findById(responseId).get();

        // A. Try metadata attribution
        if (response.getMetadata() != null) {
            try {
                Map<String, String> meta = objectMapper.readValue(response.getMetadata(), new TypeReference<>() {});
                String subjectRef = meta.get("subjectRef");
                if (subjectRef != null) {
                    Optional<PerformanceSubject> subject = performanceSubjectRepository.findByReferenceCodeAndTenantId(subjectRef, tenantId);
                    if (subject.isPresent()) return subject.get();
                }
            } catch (Exception e) {
                log.warn("Failed to parse response metadata", e);
            }
        }


        // B. Fallback: Survey Owner
        String ownerId = response.getSurvey().getUserId();
        return performanceSubjectRepository.findByUserId(ownerId).orElse(null);
    }

    private double calculateQuestionScore(QuestionScoringRule rule, String answerValue) {
        if (answerValue == null) return 0.0;

        double score = 0.0;
        if (rule.getScoringStrategy() == ScoringStrategy.DIRECT_VALUE) {
            try {
                score = Double.parseDouble(answerValue);
            } catch (NumberFormatException e) {
                // ignore
            }
        } else if (rule.getScoringStrategy() == ScoringStrategy.OPTION_MAP) {
            try {
                Map<String, Double> map = objectMapper.readValue(rule.getOptionScoreMap(), new TypeReference<>() {});
                score = map.getOrDefault(answerValue, 0.0);
            } catch (Exception e) {
                log.error("Error parsing score map", e);
            }
        }
        
        return score * rule.getWeight();
    }
}
