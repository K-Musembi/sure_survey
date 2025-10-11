package com.survey_engine.rewards.repository;

import com.survey_engine.rewards.models.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, UUID> {
}