package com.survey_engine.payments.repository;

import com.survey_engine.payments.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Transaction} entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Finds all Transactions associated with a given PaymentEvent ID and tenant ID.
     *
     * @param paymentId The UUID of the parent PaymentEvent.
     * @param tenantId The ID of the tenant.
     * @return A list of associated transactions.
     */
    List<Transaction> findByPaymentIdAndTenantId(UUID paymentId, Long tenantId);

    /**
     * Finds a Transaction by its unique gateway transaction identifier and tenant ID.
     *
     * @param gatewayTransactionId The unique identifier from the payment gateway.
     * @param tenantId The ID of the tenant.
     * @return An Optional containing the found Transaction or empty if not found.
     */
    Optional<Transaction> findByGatewayTransactionIdAndTenantId(String gatewayTransactionId, Long tenantId);
}
