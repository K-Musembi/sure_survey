package com.survey_engine.payments.repository;

import com.survey_engine.payments.models.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link PaymentEvent} entity.
 */
@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {

    /**
     * Finds a PaymentEvent entity by its idempotency key and tenant ID.
     *
     * @param idempotencyKey The unique key provided by the client.
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found PaymentEvent or empty if not found.
     */
    Optional<PaymentEvent> findByIdempotencyKeyAndTenantId(String idempotencyKey, Long tenantId);

    /**
     * Finds a PaymentEvent entity by the gateway's transaction identifier and tenant ID.
     *
     * @param gatewayTransactionId The unique identifier from the payment gateway (e.g., PayStack's reference).
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found PaymentEvent or empty if not found.
     */
    Optional<PaymentEvent> findByGatewayTransactionIdAndTenantId(String gatewayTransactionId, Long tenantId);

    /**
     * Finds all PaymentEvent entities for a given user ID and tenant ID.
     *
     * @param userId The ID of the user.
     * @param tenantId The ID of the tenant.
     * @return A list of payments for that user.
     */
    List<PaymentEvent> findByUserIdAndTenantId(String userId, Long tenantId);

    /**
     * Finds a PaymentEvent entity by the gateway's transaction identifier.
     *
     * @param gatewayTransactionId The unique identifier from the payment gateway (e.g., PayStack's reference).
     * @return An Optional containing the found PaymentEvent or empty if not found.
     */
    Optional<PaymentEvent> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Finds a PaymentEvent by gateway reference with a pessimistic write lock.
     * Used by webhook processing to prevent duplicate concurrent processing.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT pe FROM PaymentEvent pe WHERE pe.gatewayTransactionId = :ref")
    Optional<PaymentEvent> findByGatewayTransactionIdForUpdate(@org.springframework.data.repository.query.Param("ref") String gatewayTransactionId);
}
