package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.TenantWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantWalletRepository extends JpaRepository<TenantWallet, UUID> {

    Optional<TenantWallet> findByTenantId(Long tenantId);

    /**
     * Finds a wallet by tenant ID with a pessimistic write lock to prevent race conditions during balance updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TenantWallet> findByTenantIdAndIdIsNotNull(Long tenantId);
}