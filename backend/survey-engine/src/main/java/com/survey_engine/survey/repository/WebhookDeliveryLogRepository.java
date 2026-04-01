package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.WebhookDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, UUID> {

    List<WebhookDeliveryLog> findBySubscriptionIdOrderByDeliveredAtDesc(UUID subscriptionId);
}
