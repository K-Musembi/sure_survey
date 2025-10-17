package com.survey_engine.rewards.service.reward_provider;

import com.africastalking.AirtimeService;
import com.africastalking.Callback;
import com.africastalking.airtime.AirtimeResponse;
import com.survey_engine.common.events.SmsNotificationEvent;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.RewardTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A {@link RewardProvider} for disbursing AIRTIME and DATA_BUNDLE rewards
 * using the Africa's Talking API.
 * The external network call is executed asynchronously using the SDK's native callback.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AfricasTalkingAirtimeProvider implements RewardProvider {

    private final AirtimeService airtimeService;
    private final RewardTransactionService rewardTransactionService;
    private final RewardRepository rewardRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean supports(RewardType rewardType) {
        return rewardType == RewardType.AIRTIME || rewardType == RewardType.DATA_BUNDLE;
    }

    /**
     * Asynchronously handles the disbursement of airtime using the SDK's callback mechanism.
     *
     * @param reward The {@link Reward} configuration object.
     * @param responderId The recipient's phone number.
     */
    @Override
    public void disburse(Reward reward, String responderId) {
        log.info("Queueing disbursement of {} for rewardId: {} to responderId: {}", reward.getRewardType(), reward.getId(), responderId);

        final RewardTransaction transaction = rewardTransactionService.createPendingTransaction(
                reward.getId(),
                responderId, // participantId can be the phone number here
                responderId
        );

        Callback<AirtimeResponse> callback = new Callback<>() {
            @Override
            public void onSuccess(AirtimeResponse response) {
                processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, response, null);
            }

            @Override
            public void onFailure(Throwable error) {
                log.error("Asynchronous airtime disbursement failed for transactionId: {}", transaction.getId(), error);
                processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, null, error);
            }
        };

        // The send method is asynchronous and handles exceptions in the onFailure callback.
        airtimeService.send(responderId, reward.getCurrency(), reward.getAmountPerRecipient().floatValue(), callback);
    }

    /**
     * Processes the result of the airtime disbursement API call in a new transaction.
     * This method is called back by the SDK's async callback.
     *
     * @param transactionId The ID of the pending transaction.
     * @param rewardId The ID of the reward campaign.
     * @param phoneNumber The recipient's phone number.
     * @param response The response from the AT API (nullable).
     * @param error The error from the AT API call (nullable).
     */
    @Transactional
    public void processDisbursementOutcome(UUID transactionId, UUID rewardId, String phoneNumber, AirtimeResponse response, Throwable error) {
        try {
            if (error != null || response == null || !response.errorMessage.equalsIgnoreCase("none")) {
                String reason = (error != null) ? error.getMessage() : (response != null ? response.errorMessage : "Unknown failure");
                handleFailure(transactionId, phoneNumber, reason);
                return;
            }

            if (response.responses.isEmpty()) {
                handleFailure(transactionId, phoneNumber, "No response entry from provider.");
                return;
            }

            // Use the correct nested class: AirtimeResponse.Response
            AirtimeResponse.AirtimeEntry entry = response.responses.get(0);

            // Per AT docs, "Sent" is a valid success status for when the request is queued.
            if (entry.status.equalsIgnoreCase("Sent") || entry.status.equalsIgnoreCase("Success")) {
                log.info("Successfully disbursed airtime for transactionId: {}", transactionId);
                rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.SUCCESS, entry.requestId, null);

                Reward reward = rewardRepository.findById(rewardId)
                        .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

                reward.setRemainingRewards(reward.getRemainingRewards() - 1);
                if (reward.getRemainingRewards() <= 0) {
                    reward.setStatus(RewardStatus.DEPLETED);
                    log.info("Reward campaign {} has been depleted.", reward.getId());
                }
                rewardRepository.save(reward);

                String successMessage = String.format("You have received %s of airtime for completing our survey. Thank you!", entry.amount);
                eventPublisher.publishEvent(new SmsNotificationEvent(phoneNumber, successMessage));

            } else {
                handleFailure(transactionId, phoneNumber, entry.errorMessage);
            }
        } catch (Exception e) {
            log.error("Critical error during disbursement outcome processing for transactionId: {}. Manual intervention may be required.", transactionId, e);
            try {
                handleFailure(transactionId, phoneNumber, "Internal processing error: " + e.getMessage());
            } catch (Exception finalEx) {
                log.error("Failed to even mark transaction {} as failed. CRITICAL.", transactionId, finalEx);
            }
        }
    }

    private void handleFailure(UUID transactionId, String phoneNumber, String reason) {
        log.error("Failed to disburse airtime for transactionId: {}. Reason: {}", transactionId, reason);
        rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.FAILED, null, reason);

        String failureMessage = "We were unable to process your airtime reward at this time. Please contact support for assistance.";
        eventPublisher.publishEvent(new SmsNotificationEvent(phoneNumber, failureMessage));
    }
}