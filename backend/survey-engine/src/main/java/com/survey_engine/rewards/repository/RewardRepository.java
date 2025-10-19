package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.Reward;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {
    /**
     * Finds a Reward entity by its survey ID and tenant ID.
     * @param surveyId The ID of the survey.
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found Reward or empty if not found.
     */
    Optional<Reward> findBySurveyIdAndTenantId(String surveyId, Long tenantId);

    /**
     * Finds all Reward entities for a given user ID and tenant ID.
     * @param userId The ID of the user.
     * @param tenantId The ID of the tenant.
     * @return A list of rewards for that user and tenant.
     */
    List<Reward> findByUserIdAndTenantId(String userId, Long tenantId);

    /**
     * Finds a Reward entity by its ID with a pessimistic write lock.
     * @param id The ID of the reward.
     * @return An Optional containing the found Reward or empty if not found.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reward> findById(UUID id);

    /**
     * Finds a Reward entity by its ID and tenant ID.
     * @param id The ID of the reward.
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found Reward or empty if not found.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reward> findByIdAndTenantId(UUID id, Long tenantId);
}