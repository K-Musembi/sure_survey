package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.SurveyMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyMilestoneRepository extends JpaRepository<SurveyMilestone, Long> {

    List<SurveyMilestone> findBySurveyIdOrderByThresholdPctAsc(Long surveyId);

    void deleteBySurveyId(Long surveyId);
}
