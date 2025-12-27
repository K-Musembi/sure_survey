package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.PlanGatewayMapping;
import com.survey_engine.billing.models.enums.PaymentGatewayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanGatewayMappingRepository extends JpaRepository<PlanGatewayMapping, Long> {
    
    Optional<PlanGatewayMapping> findByPlanIdAndGatewayType(Long planId, PaymentGatewayType gatewayType);
    
    Optional<PlanGatewayMapping> findByGatewayPlanCodeAndGatewayType(String gatewayPlanCode, PaymentGatewayType gatewayType);
}
