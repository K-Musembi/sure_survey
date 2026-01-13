package com.survey_engine.performance_survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.performance_survey.dto.QuestionScoringRuleRequest;
import com.survey_engine.performance_survey.dto.QuestionScoringRuleResponse;
import com.survey_engine.performance_survey.dto.SurveyScoringSchemaRequest;
import com.survey_engine.performance_survey.dto.SurveyScoringSchemaResponse;
import com.survey_engine.performance_survey.models.scoring.QuestionScoringRule;
import com.survey_engine.performance_survey.models.scoring.SurveyScoringSchema;
import com.survey_engine.performance_survey.repository.QuestionScoringRuleRepository;
import com.survey_engine.performance_survey.repository.SurveyScoringSchemaRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringConfigurationService {

    private final SurveyScoringSchemaRepository schemaRepository;
    private final QuestionScoringRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;
    private final UserApi userApi;

    @Transactional
    public SurveyScoringSchemaResponse createOrUpdateSchema(SurveyScoringSchemaRequest request) {
        SurveyScoringSchema schema = schemaRepository.findBySurveyId(request.surveyId())
                .orElse(new SurveyScoringSchema());

        schema.setSurveyId(request.surveyId());
        schema.setTenantId(userApi.getTenantId());
        if (request.defaultQuestionWeight() != null) schema.setDefaultQuestionWeight(request.defaultQuestionWeight());
        if (request.targetScore() != null) schema.setTargetScore(request.targetScore());

        SurveyScoringSchema savedSchema = schemaRepository.save(schema);

        // Clear existing rules if update (simple strategy)
        // Ideally we would merge, but full replacement is safer for consistency
        List<QuestionScoringRule> existingRules = ruleRepository.findBySchemaId(savedSchema.getId());
        ruleRepository.deleteAll(existingRules);

        List<QuestionScoringRule> newRules = request.rules().stream()
                .map(r -> mapToEntity(r, savedSchema))
                .collect(Collectors.toList());
        
        List<QuestionScoringRule> savedRules = ruleRepository.saveAll(newRules);

        return mapToResponse(savedSchema, savedRules);
    }

    @Transactional(readOnly = true)
    public SurveyScoringSchemaResponse getSchema(Long surveyId) {
        SurveyScoringSchema schema = schemaRepository.findBySurveyId(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Scoring schema not found for survey " + surveyId));
        
        List<QuestionScoringRule> rules = ruleRepository.findBySchemaId(schema.getId());
        return mapToResponse(schema, rules);
    }
    
    @Transactional(readOnly = true)
    public SurveyScoringSchema findSchemaEntity(Long surveyId) {
        return schemaRepository.findBySurveyId(surveyId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<QuestionScoringRule> findRulesEntity(UUID schemaId) {

        return ruleRepository.findBySchemaId(schemaId);
    }

    private QuestionScoringRule mapToEntity(QuestionScoringRuleRequest req, SurveyScoringSchema schema) {
        QuestionScoringRule rule = new QuestionScoringRule();
        rule.setSchema(schema);
        rule.setTenantId(schema.getTenantId());
        rule.setQuestionId(req.questionId());
        rule.setWeight(req.weight() != null ? req.weight() : schema.getDefaultQuestionWeight());
        rule.setScoringStrategy(req.scoringStrategy());
        
        if (req.optionScoreMap() != null) {
            try {
                rule.setOptionScoreMap(objectMapper.writeValueAsString(req.optionScoreMap()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize option score map", e);
            }
        }
        return rule;
    }

    private SurveyScoringSchemaResponse mapToResponse(SurveyScoringSchema schema, List<QuestionScoringRule> rules) {
        List<QuestionScoringRuleResponse> ruleResponses = rules.stream()
                .map(this::mapToRuleResponse)
                .collect(Collectors.toList());

        return new SurveyScoringSchemaResponse(
                schema.getId(),
                schema.getSurveyId(),
                schema.getDefaultQuestionWeight(),
                schema.getTargetScore(),
                ruleResponses
        );
    }

    private QuestionScoringRuleResponse mapToRuleResponse(QuestionScoringRule rule) {
        Map<String, Double> map = Collections.emptyMap();
        if (rule.getOptionScoreMap() != null) {
            try {
                map = objectMapper.readValue(rule.getOptionScoreMap(), new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Failed to parse option score map", e);
            }
        }
        return new QuestionScoringRuleResponse(
                rule.getId(),
                rule.getQuestionId(),
                rule.getWeight(),
                rule.getScoringStrategy(),
                map
        );
    }
}