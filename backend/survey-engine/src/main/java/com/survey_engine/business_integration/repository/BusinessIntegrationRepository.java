package com.survey_engine.business_integration.repository;

import com.survey_engine.business_integration.models.BusinessIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessIntegrationRepository extends JpaRepository<BusinessIntegration, UUID> {

    List<BusinessIntegration> findByTenantId(Long tenantId);
}
