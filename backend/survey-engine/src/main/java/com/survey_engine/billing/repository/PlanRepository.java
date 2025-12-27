package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Plan} entity.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Finds a Plan entity by its name.
     *
     * @param name The name of the plan.
     * @return An {@link Optional} containing the found Plan or empty if not found.
     */
    Optional<Plan> findByName(String name);

}