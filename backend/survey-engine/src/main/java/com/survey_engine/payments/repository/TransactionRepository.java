package com.payments.payments.repository;

import com.payments.payments.models.Transaction;
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
     * Finds all Transactions associated with a given PaymentEvent ID.
     *
     * @param paymentId The UUID of the parent PaymentEvent.
     * @return A list of associated transactions.
     */
    List<Transaction> findByPaymentId(UUID paymentId);

    /**
     * Finds a Transaction by its unique gateway transaction identifier.
     *
     * @param gatewayTransactionId The unique identifier from the payment gateway.
     * @return An Optional containing the found Transaction or empty if not found.
     */
    Optional<Transaction> findByGatewayTransactionId(String gatewayTransactionId);
}
