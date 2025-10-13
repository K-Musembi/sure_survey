package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link LoyaltyTransaction} entity.
 */
@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {
    /**
     * Finds all loyalty transactions for a specific loyalty account.
     *
     * @param loyaltyAccountId The UUID of the loyalty account.
     * @return A list of loyalty transactions, ordered by creation date.
     */
    List<LoyaltyTransaction> findByLoyaltyAccountId(UUID loyaltyAccountId);
}