package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {

    List<WebhookSubscription> findByTenantId(Long tenantId);

    List<WebhookSubscription> findByTenantIdAndActiveTrue(Long tenantId);

    List<WebhookSubscription> findByActiveTrue();
}
