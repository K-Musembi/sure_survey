package com.survey_engine.survey.service;

import com.survey_engine.billing.BillingApi;
import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.survey.dto.cost.SurveyCostCalculationRequest;
import com.survey_engine.survey.dto.cost.SurveyCostCalculationResponse;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class SurveyCostService {

    private final SystemSettingRepository systemSettingRepository;
    private final BillingApi billingApi;
    private final UserApi userApi;

    @Transactional(readOnly = true)
    public SurveyCostCalculationResponse calculateCost(SurveyCostCalculationRequest request, Long tenantId, Long userId) {
        BigDecimal costPerRespondent = getCostPerRespondent();
        BigDecimal smsCostPerMessage = getSmsCostPerMessage();

        Integer targetRespondents = request.targetRespondents();
        BigDecimal estimatedCost;

        if (targetRespondents != null && targetRespondents > 0) {
            estimatedCost = costPerRespondent.multiply(new BigDecimal(targetRespondents));
        } else if (request.budget() != null && request.budget().compareTo(BigDecimal.ZERO) > 0) {
            estimatedCost = request.budget();
            targetRespondents = request.budget().divide(costPerRespondent, 0, RoundingMode.FLOOR).intValue();
            // Re-adjust to avoid fractional cost dust
            estimatedCost = costPerRespondent.multiply(new BigDecimal(targetRespondents));
        } else {
            targetRespondents = 0;
            estimatedCost = BigDecimal.ZERO;
        }

        // SMS distribution cost (independent of respondent activation cost)
        Integer smsContactCount = request.smsContactCount();
        BigDecimal smsCost = BigDecimal.ZERO;
        if (smsContactCount != null && smsContactCount > 0) {
            smsCost = smsCostPerMessage.multiply(new BigDecimal(smsContactCount));
        }

        BigDecimal totalCost = estimatedCost.add(smsCost);
        BigDecimal walletBalance = billingApi.getWalletBalance(tenantId, userId);
        boolean isSufficient = walletBalance.compareTo(totalCost) >= 0;
        BigDecimal requiredTopUp = isSufficient ? BigDecimal.ZERO : totalCost.subtract(walletBalance);

        return new SurveyCostCalculationResponse(
                targetRespondents,
                estimatedCost,
                costPerRespondent,
                smsContactCount,
                smsCostPerMessage,
                smsCost,
                totalCost,
                walletBalance,
                isSufficient,
                requiredTopUp
        );
    }

    public BigDecimal getCostPerRespondent() {
        return systemSettingRepository.findByKey(SettingKey.ENTERPRISE_SURVEY_COST_PER_RESPONDENT)
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("5.00"));
    }

    public BigDecimal getSmsCostPerMessage() {
        return systemSettingRepository.findByKey(SettingKey.SMS_COST_PER_MESSAGE)
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("2.00"));
    }
}
