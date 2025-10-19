package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.RewardRequest;
import com.survey_engine.rewards.dto.RewardResponse;
import com.survey_engine.rewards.models.Reward;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.repository.RewardRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    @Transactional
    public RewardResponse createReward(RewardRequest rewardRequest, String userId) {
        Long tenantId = TenantContext.getTenantId();
        rewardRepository.findBySurveyIdAndTenantId(rewardRequest.surveyId(), tenantId).ifPresent(r -> {
            throw new DataIntegrityViolationException("A reward for survey " + rewardRequest.surveyId() + " already exists.");
        });

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

        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

    @Transactional(readOnly = true)
    public RewardResponse findRewardBySurveyId(String surveyId) {
        Long tenantId = TenantContext.getTenantId();
        return rewardRepository.findBySurveyIdAndTenantId(surveyId, tenantId)
                .map(this::mapToRewardResponse)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found for surveyId: " + surveyId));
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> findRewardsByUserId(String userId) {
        Long tenantId = TenantContext.getTenantId();
        return rewardRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(this::mapToRewardResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RewardResponse cancelReward(UUID rewardId, String userId) {
        Long tenantId = TenantContext.getTenantId();
        Reward reward = rewardRepository.findByIdAndTenantId(rewardId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));

        if (!reward.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to cancel this reward.");
        }

        if (reward.getStatus() == RewardStatus.DEPLETED) {
            throw new IllegalStateException("Cannot cancel a depleted reward.");
        }

        reward.setStatus(RewardStatus.CANCELLED);
        Reward savedReward = rewardRepository.save(reward);
        return mapToRewardResponse(savedReward);
    }

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
