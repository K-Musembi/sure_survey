package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.Subscription;
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
    Optional<Subscription> findByTenantId(String tenantId);

    /**
     * Finds a Subscription entity by its Paystack subscription ID.
     *
     * @param paystackSubscriptionId The Paystack subscription ID.
     * @return An {@link Optional} containing the found Subscription or empty if not found.
     */
    Optional<Subscription> findByPaystackSubscriptionId(String paystackSubscriptionId);
}