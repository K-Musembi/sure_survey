package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.Survey;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * Method to find survey by name and tenant ID.
     * @param name - survey name
     * @param tenantId - tenant ID
     * @return - Optional of Survey
     */
    Optional<Survey> findByNameAndTenantId(String name, Long tenantId);

    /**
     * Method to find all surveys by tenant ID.
     * It uses an EntityGraph to fetch associated questions eagerly.
     * @param tenantId - tenant ID
     * @return - List of surveys
     */
    @EntityGraph(value = "Survey.withQuestions")
    List<Survey> findByTenantId(Long tenantId);

    /**
     * Method to find all surveys by tenant ID and a list of user IDs.
     * It uses an EntityGraph to fetch associated questions eagerly.
     * @param tenantId - tenant ID
     * @param userIds - list of user IDs
     * @return - List of surveys
     */
    @EntityGraph(value = "Survey.withQuestions")
    List<Survey> findByTenantIdAndUserIdIn(Long tenantId, List<String> userIds);

    /**
     * Finds a survey by its ID, eagerly fetching the associated questions.
     *
     * @param id The ID of the survey.
     * @return An {@link Optional} containing the survey if found.
     */
    @EntityGraph(value = "Survey.withQuestions")
    @Override
    Optional<Survey> findById(Long id);
}