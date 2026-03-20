package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.BranchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRuleRepository extends JpaRepository<BranchRule, Long> {

    List<BranchRule> findBySurveyIdAndSourceQuestionIdAndActiveTrueOrderByPriorityAsc(
            Long surveyId, Long sourceQuestionId);

    List<BranchRule> findBySurveyIdOrderBySourceQuestionIdAscPriorityAsc(Long surveyId);

    void deleteBySurveyId(Long surveyId);
}
