package com.survey_engine.rewards.service.provider;

import com.africastalking.AirtimeService;
import com.africastalking.airtime.AirtimeResponse;
import com.survey_engine.common.events.SmsNotificationRequested;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link RewardProvider} for disbursing AIRTIME and DATA_BUNDLE rewards
 * using the Africa's Talking API.
 * The external network call is executed asynchronously.
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
     * Asynchronously handles the disbursement of airtime.
     * It creates a pending transaction and then schedules the blocking network call
     * on a separate thread pool, ensuring the caller (event listener) is not blocked.
     *
     * @param reward The {@link Reward} configuration object.
     * @param responderId The recipient's phone number.
     */
    @Override
    public void disburse(Reward reward, String responderId) {
        log.info("Queueing disbursement of {} for rewardId: {} to responderId: {}", reward.getRewardType(), reward.getId(), responderId);

        // 1. Create the pending transaction in a synchronous, transactional method.
        RewardTransaction transaction = rewardTransactionService.createPendingTransaction(
                reward.getId(),
                responderId, // participantId can be the phone number here
                responderId
        );

        // 2. Prepare the data for the async operation.
        Map<String, String> recipients = new HashMap<>();
        String amountWithCurrency = reward.getCurrency() + " " + reward.getAmountPerRecipient().toPlainString();
        recipients.put(responderId, amountWithCurrency);

        // 3. Execute the blocking network call asynchronously.
        Mono.fromCallable(() -> airtimeService.send(recipients)) // Wrap the blocking call
                .subscribeOn(Schedulers.boundedElastic()) // Schedule it on a dedicated thread pool for blocking tasks
                .doOnSuccess(response -> {
                    // On success, process the outcome in a new transaction.
                    processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, response, null);
                })
                .doOnError(error -> {
                    // On failure, process the outcome in a new transaction.
                    log.error("Asynchronous airtime disbursement failed for transactionId: {}", transaction.getId(), error);
                    processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, null, error);
                })
                .subscribe(); // Fire and forget.
    }

    /**
     * Processes the result of the airtime disbursement API call in a new transaction.
     * This method is called back by the reactive pipeline.
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
            if (error == null && response != null && response.getErrorMessage().equalsIgnoreCase("none")) {
                // SUCCESS CASE
                log.info("Successfully disbursed airtime for transactionId: {}", transactionId);
                rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.SUCCESS, null, null);

                Reward reward = rewardRepository.findById(rewardId)
                        .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

                reward.setRemainingRewards(reward.getRemainingRewards() - 1);
                if (reward.getRemainingRewards() <= 0) {
                    reward.setStatus(RewardStatus.DEPLETED);
                    log.info("Reward campaign {} has been depleted.", reward.getId());
                }
                rewardRepository.save(reward);

                String amount = response.getResponses().get(0).getAmount();
                String successMessage = String.format("You have received %s of airtime for completing our survey. Thank you!", amount);
                eventPublisher.publishEvent(new SmsNotificationRequested(phoneNumber, successMessage));

            } else {
                // FAILURE CASE
                String reason = (error != null) ? error.getMessage() : (response != null ? response.getErrorMessage() : "Unknown failure");
                log.error("Failed to disburse airtime for transactionId: {}. Reason: {}", transactionId, reason);
                rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.FAILED, null, reason);

                String failureMessage = "We were unable to process your airtime reward at this time. Please contact support for assistance.";
                eventPublisher.publishEvent(new SmsNotificationRequested(phoneNumber, failureMessage));
            }
        } catch (Exception e) {
            log.error("Critical error during disbursement outcome processing for transactionId: {}. Manual intervention may be required.", transactionId, e);
            // If this block fails, the transaction might be stuck in PENDING.
        }
    }
}