package com.survey_engine.survey.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.survey.dto.BranchRuleRequest;
import com.survey_engine.survey.dto.BranchRuleResponse;
import com.survey_engine.survey.dto.MilestoneRequest;
import com.survey_engine.survey.models.BranchRule;
import com.survey_engine.survey.common.enums.ConditionType;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.models.SurveyMilestone;
import com.survey_engine.survey.repository.BranchRuleRepository;
import com.survey_engine.survey.repository.QuestionRepository;
import com.survey_engine.survey.repository.SurveyMilestoneRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages branch rules and milestones for surveys.
 * Also contains the branch rule evaluation engine used at response-time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchRuleService {

    private final BranchRuleRepository branchRuleRepository;
    private final SurveyMilestoneRepository milestoneRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public BranchRuleResponse createRule(Long surveyId, BranchRuleRequest request) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("SURVEY_NOT_FOUND", "Survey not found: " + surveyId));

        if (!questionRepository.existsByIdAndSurveyId(request.sourceQuestionId(), surveyId)) {
            throw new BusinessRuleException("BRANCH_RULE_SOURCE_QUESTION_INVALID",
                    "Source question does not belong to survey " + surveyId);
        }

        if (request.targetQuestionId() != null &&
                !questionRepository.existsByIdAndSurveyId(request.targetQuestionId(), surveyId)) {
            throw new BusinessRuleException("BRANCH_RULE_TARGET_QUESTION_INVALID",
                    "Target question does not belong to survey " + surveyId);
        }

        BranchRule rule = new BranchRule();
        rule.setSurvey(survey);
        rule.setSourceQuestionId(request.sourceQuestionId());
        rule.setConditionType(request.conditionType());
        rule.setConditionValue(request.conditionValue());
        rule.setTargetQuestionId(request.targetQuestionId());
        rule.setPriority(request.priority());
        rule.setActive(true);

        return BranchRuleResponse.from(branchRuleRepository.save(rule));
    }

    public List<BranchRuleResponse> getRulesForSurvey(Long surveyId) {
        return branchRuleRepository.findBySurveyIdOrderBySourceQuestionIdAscPriorityAsc(surveyId)
                .stream().map(BranchRuleResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        if (!branchRuleRepository.existsById(ruleId)) {
            throw new ResourceNotFoundException("BRANCH_RULE_NOT_FOUND", "Branch rule not found: " + ruleId);
        }
        branchRuleRepository.deleteById(ruleId);
    }

    @Transactional
    public SurveyMilestone createMilestone(Long surveyId, MilestoneRequest request) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("SURVEY_NOT_FOUND", "Survey not found: " + surveyId));

        SurveyMilestone milestone = new SurveyMilestone();
        milestone.setSurvey(survey);
        milestone.setThresholdPct(request.thresholdPct());
        milestone.setMessage(request.message());
        milestone.setBadgeTypeId(request.badgeTypeId());
        return milestoneRepository.save(milestone);
    }

    public List<SurveyMilestone> getMilestonesForSurvey(Long surveyId) {
        return milestoneRepository.findBySurveyIdOrderByThresholdPctAsc(surveyId);
    }

    /**
     * Given the just-answered question, the selected option index, and the
     * accumulated category scores for this session, resolves the next question ID.
     * Returns null → end the survey.
     * Returns -1L → no rule matched, caller should use linear progression.
     */
    public Long resolveNextQuestion(Long surveyId, Long answeredQuestionId, Integer selectedOptionIndex,
                                    Map<String, Double> categoryScores, double totalScore) {
        List<BranchRule> rules = branchRuleRepository
                .findBySurveyIdAndSourceQuestionIdAndActiveTrueOrderByPriorityAsc(
                        surveyId, answeredQuestionId);

        for (BranchRule rule : rules) {
            if (matches(rule, selectedOptionIndex, categoryScores, totalScore)) {
                log.debug("Branch rule {} matched for survey={} question={}",
                        rule.getId(), surveyId, answeredQuestionId);
                return rule.getTargetQuestionId(); // null = end survey
            }
        }
        return -1L; // sentinel: no rule matched
    }

    private boolean matches(BranchRule rule, Integer selectedOptionIndex, Map<String, Double> categoryScores, double totalScore) {
        return switch (rule.getConditionType()) {
            case ALWAYS -> true;
            case ANSWER_EQUALS -> {
                Integer expected = conditionInt(rule.getConditionValue(), "optionIndex");
                yield expected != null && expected.equals(selectedOptionIndex);
            }
            case SCORE_LT -> {
                Double threshold = conditionDouble(rule.getConditionValue(), "threshold");
                yield threshold != null && totalScore < threshold;
            }
            case SCORE_GT -> {
                Double threshold = conditionDouble(rule.getConditionValue(), "threshold");
                yield threshold != null && totalScore > threshold;
            }
            case SCORE_CATEGORY_LT -> {
                Double threshold = conditionDouble(rule.getConditionValue(), "threshold");
                String cat = conditionString(rule.getConditionValue(), "category");
                double catScore = categoryScores.getOrDefault(cat, 0.0);
                yield threshold != null && cat != null && catScore < threshold;
            }
            case SCORE_CATEGORY_GT -> {
                Double threshold = conditionDouble(rule.getConditionValue(), "threshold");
                String cat = conditionString(rule.getConditionValue(), "category");
                double catScore = categoryScores.getOrDefault(cat, 0.0);
                yield threshold != null && cat != null && catScore > threshold;
            }
        };
    }

    private Integer conditionInt(String json, String field) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.has(field) ? node.get(field).asInt() : null;
        } catch (Exception e) {
            log.warn("Failed to parse branch rule condition JSON: {}", json);
            return null;
        }
    }

    private Double conditionDouble(String json, String field) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.has(field) ? node.get(field).asDouble() : null;
        } catch (Exception e) {
            log.warn("Failed to parse branch rule condition JSON: {}", json);
            return null;
        }
    }

    private String conditionString(String json, String field) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.has(field) ? node.get(field).asText() : null;
        } catch (Exception e) {
            log.warn("Failed to parse branch rule condition JSON: {}", json);
            return null;
        }
    }
}
