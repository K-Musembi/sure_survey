package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {
    List<LoyaltyTransaction> findByLoyaltyAccountId(UUID loyaltyAccountId);
}