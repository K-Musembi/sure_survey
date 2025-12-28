package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Subscription} entity.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Finds a Subscription entity by its tenant ID.
     *
     * @param tenantId The ID of the tenant.
     * @return An {@link Optional} containing the found Subscription or empty if not found.
     */
    Optional<Subscription> findByTenantId(Long tenantId);

    /**
     * Finds the first active subscription for a given tenant, ordered by creation date.
     *
     * @param tenantId The ID of the tenant.
     * @param status The status of the subscription.
     * @return An {@link Optional} containing the earliest active subscription or empty if not found.
     */
    Optional<Subscription> findFirstByTenantIdAndStatusOrderByIdAsc(Long tenantId, SubscriptionStatus status);


    /**
     * Finds a Subscription entity by its tenant ID and user ID.
     *
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @return An {@link Optional} containing the found Subscription or empty if not found.
     */
    Optional<Subscription> findByTenantIdAndUserId(Long tenantId, Long userId);

    /**
     * Finds an active subscription by tenant ID and user ID.
     *
     * @param tenantId The ID of the tenant.
     * @param userId The ID of the user.
     * @param status The subscription status.
     * @return An {@link Optional} of Subscription.
     */
    Optional<Subscription> findByTenantIdAndUserIdAndStatus(Long tenantId, Long userId, SubscriptionStatus status);

    /**
     * Finds a Subscription entity by its Gateway subscription ID.
     *
     * @param gatewaySubscriptionId The Gateway subscription ID.
     * @return An {@link Optional} containing the found Subscription or empty if not found.
     */
    Optional<Subscription> findByGatewaySubscriptionId(String gatewaySubscriptionId);
}