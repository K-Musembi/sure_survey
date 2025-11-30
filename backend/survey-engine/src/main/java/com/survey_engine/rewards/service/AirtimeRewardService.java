package com.survey_engine.rewards.service;

import com.africastalking.airtime.AirtimeResponse;
import com.survey_engine.billing.BillingApi;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.notifications.NotificationService;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A service dedicated to processing the outcome of airtime disbursement attempts.
 * This class is separated from the provider to ensure that transactional boundaries
 * are correctly applied when processing the asynchronous callbacks from the
 * Africa's Talking API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AirtimeRewardService {

    private final RewardTransactionService rewardTransactionService;
    private final RewardRepository rewardRepository;
    private final NotificationService notificationService;
    private final UserApi userApi;
    private final BillingApi billingApi;

    /**
     * Processes the result of the airtime disbursement API call in a new transaction.
     * This method is called back by the SDK's async callback. It locks the reward
     * record to prevent race conditions when updating the remaining rewards count.
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
            // Idempotency Check: Ensure transaction hasn't already been processed
            var transaction = rewardTransactionService.findTransactionById(transactionId);
            if (transaction.status() != RewardTransactionStatus.PENDING) {
                log.warn("Transaction {} has already been processed (Status: {}). Ignoring duplicate callback.", transactionId, transaction.status());
                return;
            }

            if (error != null || response == null || !response.errorMessage.equalsIgnoreCase("none")) {
                String reason = (error != null) ? error.getMessage() : (response != null ? response.errorMessage : "Unknown failure");
                handleFailure(transactionId, phoneNumber, reason, null);
                return;
            }

            if (response.responses.isEmpty()) {
                handleFailure(transactionId, phoneNumber, "No response entry from provider.", null);
                return;
            }

            AirtimeResponse.AirtimeEntry entry = response.responses.get(0);

            if (entry.status.equalsIgnoreCase("Sent") || entry.status.equalsIgnoreCase("Success")) {
                log.info("Successfully disbursed airtime for transactionId: {}", transactionId);
                rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.SUCCESS, entry.requestId, null);

                // Lock the reward to prevent race conditions
                Long tenantId = userApi.getTenantId();
                Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId + " for tenant: " + tenantId));

                if (reward.getRemainingRewards() > 0) {
                    reward.setRemainingRewards(reward.getRemainingRewards() - 1);
                    if (reward.getRemainingRewards() <= 0) {
                        reward.setStatus(RewardStatus.DEPLETED);
                        log.info("Reward campaign {} has been depleted.", reward.getId());
                    }
                    rewardRepository.save(reward);

                    // Commit the system reservation now that the reward has been successfully disbursed
                    String walletType = reward.getRewardType() == RewardType.AIRTIME ? "AIRTIME_STOCK" : "DATA_BUNDLE_STOCK";
                    billingApi.commitSystemReservation(walletType, reward.getAmountPerRecipient());

                    String successMessage = String.format("You have received %s of airtime for completing our survey. Thank you!", entry.amount);
                    notificationService.sendSms(phoneNumber, successMessage);
                } else {
                    // This case handles the race condition if another process already depleted the reward.
                    log.warn("Reward {} was already depleted. Airtime sent for transactionId {} will not be accounted for against the reward budget.", rewardId, transactionId);
                    handleFailure(transactionId, phoneNumber, "Reward campaign was depleted before this transaction could be processed.", reward);
                }

            } else {
                // We need to fetch the reward to roll back the transaction
                Long tenantId = userApi.getTenantId();
                Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId).orElse(null);
                handleFailure(transactionId, phoneNumber, entry.errorMessage, reward);
            }
        } catch (Exception e) {
            log.error("Critical error during disbursement outcome processing for transactionId: {}. Manual intervention may be required.", transactionId, e);
            try {
                handleFailure(transactionId, phoneNumber, "Internal processing error: " + e.getMessage(), null);
            } catch (Exception finalEx) {
                log.error("Failed to even mark transaction {} as failed. CRITICAL.", transactionId, finalEx);
            }
        }
    }

    /**
     * Handles the failure of an airtime disbursement.
     * It updates the transaction status to FAILED and notifies the user via SMS.
     *
     * @param transactionId The ID of the transaction that failed.
     * @param phoneNumber The recipient's phone number.
     * @param reason The reason for the failure.
     * @param reward The reward object, used for rolling back reservation.
     */
    private void handleFailure(UUID transactionId, String phoneNumber, String reason, Reward reward) {
        log.error("Failed to disburse airtime for transactionId: {}. Reason: {}", transactionId, reason);
        rewardTransactionService.updateTransactionStatus(transactionId, RewardTransactionStatus.FAILED, null, reason);

        // Rollback the system reservation so the stock is available again
        if (reward != null) {
            String walletType = reward.getRewardType() == RewardType.AIRTIME ? "AIRTIME_STOCK" : "DATA_BUNDLE_STOCK";
            billingApi.rollbackSystemReservation(walletType, reward.getAmountPerRecipient());
        }

        String failureMessage = "We were unable to process your airtime reward at this time. Please contact support for assistance.";
        notificationService.sendSms(phoneNumber, failureMessage);
    }
}
