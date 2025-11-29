package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.SystemWallet;
import com.survey_engine.billing.models.enums.SystemWalletType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemWalletRepository extends JpaRepository<SystemWallet, SystemWalletType> {

    /**
     * Finds a wallet by type with a pessimistic write lock to ensure atomic updates
     * during high-concurrency reward distribution.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SystemWallet> findByWalletType(SystemWalletType walletType);
}