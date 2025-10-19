package com.survey_engine.rewards.service.event_listener;

import com.survey_engine.common.events.RewardDistributionEvent;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Listens for survey completion events and initiates the reward workflow
 * by publishing a more specific reward distribution event.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyEventListener {

    private final RewardRepository rewardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserApi userApi;

    /**
     * Handles the {@link SurveyCompletedEvent} from the survey module.
     * It checks if a reward is configured and active for the completed survey,
     * and if so, publishes a {@link RewardDistributionEvent} event to trigger
     * the fulfillment process.
     *
     * @param event The event fired when a survey response is successfully completed.
     */
    @EventListener
    @Transactional(readOnly = true) // This listener should only read and publish a new event.
    public void handleSurveyCompletion(SurveyCompletedEvent event) {
        log.info("Received SurveyCompletedEvent for surveyId: {}", event.surveyId());

        Long tenantId = userApi.getTenantId();
        Optional<Reward> rewardOpt = rewardRepository.findBySurveyIdAndTenantId(String.valueOf(event.surveyId()), tenantId);
        if (rewardOpt.isEmpty()) {
            log.info("No reward configured for surveyId: {}. Skipping reward workflow.", event.surveyId());
            return;
        }

        Reward reward = rewardOpt.get();

        // Basic validation before publishing the distribution event
        if (reward.getStatus() != RewardStatus.ACTIVE || reward.getRemainingRewards() <= 0) {
            log.warn("Reward for surveyId: {} is not eligible for distribution. Status: {}, Remaining Rewards: {}. Skipping.",
                    event.surveyId(), reward.getStatus(), reward.getRemainingRewards());
            return;
        }

        // Publish a new, specific event for the rewards module to handle fulfillment.
        RewardDistributionEvent distributionEvent = new RewardDistributionEvent(
                reward.getId(),
                event.responderId()
        );
        eventPublisher.publishEvent(distributionEvent);

        log.info("Published RewardDistributionEvent for rewardId: {} and responderId: {}",
                reward.getId(), event.responderId());
    }
}