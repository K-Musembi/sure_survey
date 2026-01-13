package com.survey_engine.performance_survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.performance_survey.events.ScoreCalculatedEvent;
import com.survey_engine.performance_survey.models.scoring.PerformanceRecord;
import com.survey_engine.performance_survey.models.scoring.QuestionScoringRule;
import com.survey_engine.performance_survey.models.scoring.SurveyScoringSchema;
import com.survey_engine.performance_survey.models.scoring.enums.ScoringStrategy;
import com.survey_engine.performance_survey.models.structure.OrgUnit;
import com.survey_engine.performance_survey.repository.PerformanceRecordRepository;
import com.survey_engine.survey.SurveyApi;
import com.survey_engine.survey.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceExecutionService {

    private final ScoringConfigurationService configService;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final HierarchyService hierarchyService;
    private final SurveyApi surveyApi; // Need this to fetch actual answers
    private final ObjectMapper objectMapper;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

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

        // 2. Fetch answers (Raw implementation: need to fetch from Survey Module DB via API/Repo)
        // Since SurveyApi interface was limited, we might need to extend it or use the Repository directly if allowed.
        // The plan allows using SurveyApi. SurveyApi exposes `getSurveyResponseTexts` but that's text.
        // Ideally, we need the structured answers. 
        // `SurveyApi.getResponseRepository()` is available.
        var responseOpt = surveyApi.getResponseRepository().findById(event.responseId());
        if (responseOpt.isEmpty()) return;
        var response = responseOpt.get();

        // 3. Calculate Score
        List<QuestionScoringRule> rules = configService.findRulesEntity(schema.getId());
        Map<Long, QuestionScoringRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(QuestionScoringRule::getQuestionId, r -> r));

        double totalScore = 0.0;
        double maxPossibleScore = 0.0; // To normalize if needed, though schema has targetScore.

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

        // 5. Determine Org Unit
        // The `responderId` in the event is the participant. 
        // BUT for performance surveys, usually the Subject is the one we track.
        // If it's a "Performance Review" of an Agent, the Agent is the subject.
        // We need to know who the SUBJECT is.
        // In the current `Survey` model, `userId` is the creator.
        // If the survey is "Feedback for Agent X", we need to link Agent X.
        // Assumption: The `subjectUserId` is either the survey owner OR passed via a custom logic.
        // For this MVP, let's assume the Survey Creator (userId) is the one being evaluated (e.g. CSAT for their service).
        // OR the respondent IS the subject (Self-Assessment).
        // Let's stick to: Survey Owner is the Subject (e.g. Agent sends link to customer).
        String subjectUserId = response.getSurvey().getUserId();
        
        OrgUnit orgUnit = hierarchyService.getOrgUnitForUser(subjectUserId);

        // 6. Save Record
        PerformanceRecord record = new PerformanceRecord();
        record.setSurveyId(event.surveyId());
        record.setResponseId(event.responseId());
        record.setSubjectUserId(subjectUserId);
        record.setEvaluatorUserId(event.responderId());
        record.setRawScore(totalScore);
        record.setNormalizedScore(normalized);
        record.setOrgUnitId(orgUnit != null ? orgUnit.getId() : null);
        record.setRecordedAt(LocalDateTime.now());
        record.setTenantId(schema.getTenantId()); // Inherit tenant

        performanceRecordRepository.save(record);
        log.info("Saved performance record for user {}: Score {}", subjectUserId, normalized);
        
        // Publish event for Aggregation and Gamification
        eventPublisher.publishEvent(new ScoreCalculatedEvent(
                record.getId(),
                record.getSubjectUserId(),
                record.getNormalizedScore(),
                record.getOrgUnitId()
        ));
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