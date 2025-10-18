package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link LoyaltyAccount} entity.
 */
@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {
    /**
     * Finds a LoyaltyAccount by the associated user's ID and tenant ID.
     *
     * @param userId The unique identifier of the user.
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found LoyaltyAccount, or empty if not found.
     */
    Optional<LoyaltyAccount> findByUserIdAndTenantId(String userId, Long tenantId);
}