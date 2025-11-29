package com.survey_engine.rewards.service;

import com.survey_engine.billing.BillingApi;
import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.rewards.dto.RewardRequest;
import com.survey_engine.rewards.dto.RewardResponse;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing reward configurations.
 * Handles creation, retrieval, and cancellation of rewards.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserApi userApi;
    private final BillingApi billingApi;
    private final SystemSettingRepository systemSettingRepository;

    /**
     * Creates a new reward configuration for a survey, scoped by the current tenant.
     *
     * @param rewardRequest The request DTO containing reward details.
     * @param userId The ID of the user creating the reward.
     * @return A {@link RewardResponse} DTO representing the created reward.
     * @throws DataIntegrityViolationException if a reward for the given survey already exists within the tenant.
     */
    @Transactional
    public RewardResponse createReward(RewardRequest rewardRequest, String userId) {
        Long tenantId = userApi.getTenantId();
        rewardRepository.findBySurveyIdAndTenantId(rewardRequest.surveyId(), tenantId).ifPresent(r -> {
            throw new DataIntegrityViolationException("A reward for survey " + rewardRequest.surveyId() + " already exists.");
        });

        // 1. Determine Cost per Unit based on SystemSettings
        BigDecimal costPerUnit = calculateCostPerUnit(rewardRequest);

        // 2. Calculate Total Cost
        BigDecimal totalCost = costPerUnit.multiply(new BigDecimal(rewardRequest.maxRecipients()));

        // 3. Check & Debit Tenant Wallet (Cash)
        try {
            billingApi.debitWallet(tenantId, totalCost, "Funding for reward campaign on survey " + rewardRequest.surveyId());
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Insufficient funds to create reward. Required: " + totalCost + ". Please top up your wallet.");
        }

        // 4. Check & Reserve System Inventory (Stock) if applicable
        reserveSystemInventory(rewardRequest);

        // 5. Save Reward
        Reward reward = getReward(rewardRequest, userId, tenantId);
        reward.setTotal_amount(totalCost);
        reward.setAmountPerRecipient(costPerUnit); // Persist the rate at time of creation

        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

    private BigDecimal calculateCostPerUnit(RewardRequest request) {
        if (request.rewardType() == RewardType.LOYALTY_POINTS) {
            return BigDecimal.ZERO; // Loyalty points are free to issue for the tenant (internal currency)
        }
        // For Airtime, the amount per recipient IS the value.
        // For Data Bundles, we need to look up the price.
        if (request.rewardType() == RewardType.AIRTIME) {
             // Assuming standard 1:1 mapping for airtime value vs cost, or fetching a markup.
             // Let's check if there are configured retail prices for specific amounts
             if (request.amountPerRecipient().compareTo(new BigDecimal("20")) == 0) {
                 return getSettingValue(SettingKey.AIRTIME_RETAIL_PRICE_20, request.amountPerRecipient());
             }
             return request.amountPerRecipient(); 
        }
        
        // Map Data Bundle Types to Price Settings
        // This is a bit simplified. Ideally RewardType might be more granular or we check description.
        // Assuming we infer size from amount or separate field. 
        // For prototype, we will assume 'amountPerRecipient' holds the MB value for data bundles.
        if (request.rewardType() == RewardType.DATA_BUNDLE) {
            if (request.amountPerRecipient().intValue() == 20) {
                return getSettingValue(SettingKey.DATA_BUNDLE_PRICE_20MB, BigDecimal.ZERO);
            } else if (request.amountPerRecipient().intValue() == 100) {
                return getSettingValue(SettingKey.DATA_BUNDLE_PRICE_100MB, BigDecimal.ZERO);
            }
        }
        
        return request.amountPerRecipient();
    }

    private BigDecimal getSettingValue(SettingKey key, BigDecimal defaultValue) {
        return systemSettingRepository.findByKey(key)
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(defaultValue);
    }

    private void reserveSystemInventory(RewardRequest request) {
        if (request.rewardType() == RewardType.LOYALTY_POINTS) return;

        String walletType = request.rewardType() == RewardType.AIRTIME 
                ? "AIRTIME_STOCK" 
                : "DATA_BUNDLE_STOCK";
        
        // Reserve total value required
        BigDecimal totalValueToReserve = request.amountPerRecipient().multiply(new BigDecimal(request.maxRecipients()));
        billingApi.reserveSystemStock(walletType, totalValueToReserve);
    }

    /**
     * Finds a reward configuration for a specific survey, scoped by the current tenant.
     *
     * @param surveyId The ID of the survey.
     * @return A {@link RewardResponse} DTO representing the found reward.
     * @throws EntityNotFoundException if no reward is found for the given survey ID and tenant.
     */
    @Transactional(readOnly = true)
    public RewardResponse findRewardBySurveyId(String surveyId) {
        Long tenantId = userApi.getTenantId();
        return rewardRepository.findBySurveyIdAndTenantId(surveyId, tenantId)
                .map(this::mapToRewardResponse)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found for surveyId: " + surveyId));
    }

    /**
     * Finds all reward configurations created by a specific user, scoped by the current tenant.
     *
     * @param userId The ID of the user.
     * @return A list of {@link RewardResponse} DTOs for the specified user and tenant.
     */
    @Transactional(readOnly = true)
    public List<RewardResponse> findRewardsByUserId(String userId) {
        Long tenantId = userApi.getTenantId();
        return rewardRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(this::mapToRewardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancels an existing reward campaign, scoped by the current tenant and user.
     *
     * @param rewardId The ID of the reward to cancel.
     * @param userId The ID of the user attempting to cancel the reward.
     * @return A {@link RewardResponse} DTO representing the updated (cancelled) reward.
     * @throws EntityNotFoundException if the reward is not found for the given ID and tenant.
     * @throws AccessDeniedException if the user is not the owner of the reward.
     * @throws IllegalStateException if the reward is already depleted.
     */
    @Transactional
    public RewardResponse cancelReward(UUID rewardId, String userId) {
        Long tenantId = userApi.getTenantId();
        Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

        if (!reward.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to cancel this reward.");
        }

        if (reward.getStatus() == RewardStatus.DEPLETED) {
            throw new IllegalStateException("Cannot cancel a depleted reward.");
        }

        // Refund logic
        if (reward.getStatus() == RewardStatus.ACTIVE && reward.getRemainingRewards() > 0) {
            BigDecimal refundAmount = reward.getAmountPerRecipient().multiply(new BigDecimal(reward.getRemainingRewards()));
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                billingApi.creditWallet(tenantId, refundAmount, "REFUND:CANCEL:" + rewardId, "Refund for cancelled reward campaign on survey " + reward.getSurveyId());
            }
        }

        reward.setStatus(RewardStatus.CANCELLED);
        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

    /**
     * Instantiate a new Reward instance and set properties
     * @param rewardRequest - request DTO
     * @param userId - user id
     * @param tenantId - tenant id
     * @return - Reward instance
     */
    private static Reward getReward(RewardRequest rewardRequest, String userId, Long tenantId) {
        Reward reward = new Reward();
        reward.setTenantId(tenantId);
        reward.setSurveyId(rewardRequest.surveyId());
        reward.setUserId(userId);
        reward.setRewardType(rewardRequest.rewardType());
        reward.setAmountPerRecipient(rewardRequest.amountPerRecipient());
        reward.setCurrency(rewardRequest.currency());
        reward.setProvider(rewardRequest.provider());
        reward.setMaxRecipients(rewardRequest.maxRecipients());
        reward.setRemainingRewards(rewardRequest.maxRecipients());
        reward.setStatus(RewardStatus.ACTIVE);
        return reward;
    }

    /**
     * Maps a {@link Reward} entity to a {@link RewardResponse} DTO.
     *
     * @param reward The {@link Reward} entity to map.
     * @return The corresponding {@link RewardResponse} DTO.
     */
    private RewardResponse mapToRewardResponse(Reward reward) {
        return new RewardResponse(
                reward.getId(),
                reward.getSurveyId(),
                reward.getUserId(),
                reward.getRewardType(),
                reward.getAmountPerRecipient(),
                reward.getCurrency(),
                reward.getProvider(),
                reward.getMaxRecipients(),
                reward.getRemainingRewards(),
                reward.getStatus(),
                reward.getCreatedAt(),
                reward.getUpdatedAt()
        );
    }
}
