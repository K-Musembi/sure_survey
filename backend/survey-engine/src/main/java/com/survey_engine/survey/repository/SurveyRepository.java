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
     * Method to find survey by name and tenant ID.
     * @param name - survey name
     * @param tenantId - tenant ID
     * @return - Optional of Survey
     */
    Optional<Survey> findByNameAndTenantId(String name, Long tenantId);

    /**
     * Method to find all surveys by user id and tenant ID.
     * @param userId - user id
     * @param tenantId - tenant ID
     * @return - List of surveys
     */
    List<Survey> findByUserIdAndTenantId(String userId, Long tenantId);
}