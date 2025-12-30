package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.DistributionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistributionListRepository extends JpaRepository<DistributionList, UUID> {
    /**
     * Finds all DistributionList entities for a given tenant ID.
     * @param tenantId The ID of the tenant.
     * @return A list of distribution lists for that tenant.
     */
    List<DistributionList> findByTenantId(Long tenantId);

    /**
     * Finds all DistributionList entities for a given tenant ID and user ID.
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @return A list of distribution lists for that tenant and user.
     */
    List<DistributionList> findAllByTenantIdAndUserId(Long tenantId, String userId);

    /**
     * Finds a DistributionList entity by its ID, tenant ID, and user ID.
     * @param id The ID of the distribution list.
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @return An Optional containing the found DistributionList or empty if not found.
     */
    Optional<DistributionList> findByIdAndTenantIdAndUserId(UUID id, Long tenantId, String userId);
}