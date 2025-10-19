package com.survey_engine.rewards.service.reward_provider;

import com.africastalking.AirtimeService;
import com.africastalking.Callback;
import com.africastalking.airtime.AirtimeResponse;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.RewardTransaction;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.rewards.service.AirtimeRewardService;
import com.survey_engine.rewards.service.RewardTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    private final AirtimeRewardService airtimeRewardService;
    private final RewardRepository rewardRepository;


    /**
     * Checks if this provider supports the given reward type.
     *
     * @param rewardType The {@link RewardType} to check.
     * @return {@code true} if the reward type is AIRTIME or DATA_BUNDLE, {@code false} otherwise.
     */
    @Override
    public boolean supports(RewardType rewardType) {
        return rewardType == RewardType.AIRTIME || rewardType == RewardType.DATA_BUNDLE;
    }

    /**
     * Asynchronously handles the disbursement of airtime using the SDK's callback mechanism.
     *
     * @param rewardId The ID of the {@link Reward} configuration object.
     * @param responderId The recipient's phone number.
     */
    @Override
    public void disburse(UUID rewardId, String responderId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

        log.info("Queueing disbursement of {} for rewardId: {} to responderId: {}", reward.getRewardType(), reward.getId(), responderId);

        final RewardTransaction transaction = rewardTransactionService.createPendingTransaction(
                reward.getId(),
                responderId, // participantId can be the phone number here
                responderId
        );

        Callback<AirtimeResponse> callback = new Callback<>() {
            @Override
            public void onSuccess(AirtimeResponse response) {
                airtimeRewardService.processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, response, null);
            }

            @Override
            public void onFailure(Throwable error) {
                log.error("Asynchronous airtime disbursement failed for transactionId: {}", transaction.getId(), error);
                airtimeRewardService.processDisbursementOutcome(transaction.getId(), reward.getId(), responderId, null, error);
            }
        };

        // The send method is asynchronous and handles exceptions in the onFailure callback.
        airtimeService.send(responderId, reward.getCurrency(), reward.getAmountPerRecipient().floatValue(), callback);
    }
}