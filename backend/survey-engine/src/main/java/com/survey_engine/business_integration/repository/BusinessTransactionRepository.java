package com.survey_engine.business_integration.repository;

import com.survey_engine.business_integration.models.BusinessTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessTransactionRepository extends JpaRepository<BusinessTransaction, UUID> {

    Optional<BusinessTransaction> findByExternalTransactionId(String externalTransactionId);
    
    /**
     * Finds all transactions for a given integration and tenant.
     * @param integrationId The integration ID.
     * @param tenantId The tenant ID.
     * @return List of transactions.
     */
    List<BusinessTransaction> findByIntegrationIdAndTenantId(UUID integrationId, Long tenantId);
}
