package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Survey entity
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * Method to find survey by name
     * @param name - survey name
     * @return - Optional of Survey
     */
    Optional<Survey> findByName(String name);

    /**
     * Method to find all surveys by user id
     * @param userId - user id
     * @return - List of surveys
     */
    List<Survey> findByUserId(String userId);
}