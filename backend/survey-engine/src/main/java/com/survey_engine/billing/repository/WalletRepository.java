package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByTenantIdAndUserIdIsNull(Long tenantId);
    
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Finds a tenant wallet with a pessimistic write lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByTenantIdAndUserIdIsNullAndIdIsNotNull(Long tenantId);

    /**
     * Finds a user wallet with a pessimistic write lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserIdAndIdIsNotNull(Long userId);
}
