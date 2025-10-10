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
     * Finds a PaymentEvent entity by its idempotency key.
     *
     * @param idempotencyKey The unique key provided by the client.
     * @return An Optional containing the found PaymentEvent or empty if not found.
     */
    Optional<PaymentEvent> findByIdempotencyKey(String idempotencyKey);

    /**
     * Finds a PaymentEvent entity by the gateway's transaction identifier.
     *
     * @param gatewayTransactionId The unique identifier from the payment gateway (e.g., PayStack's reference).
     * @return An Optional containing the found PaymentEvent or empty if not found.
     */
    Optional<PaymentEvent> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Finds all PaymentEvent entities for a given user ID.
     *
     * @param userId The ID of the user.
     * @return A list of payments for that user.
     */
    List<PaymentEvent> findByUserId(String userId);
}
