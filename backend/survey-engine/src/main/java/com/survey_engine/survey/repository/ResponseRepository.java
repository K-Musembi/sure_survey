package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the Response entity.
 */
@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {

    /**
     * Finds all Responses associated with a specific Survey.
     * @param surveyId The ID of the Survey.
     * @return A list of Responses.
     */
    List<Response> findBySurveyId(Long surveyId);

    /**
     * Counts the number of responses for a given survey.
     * @param surveyId The ID of the Survey.
     * @return The count of responses.
     */
    long countBySurveyId(Long surveyId);
}
