package com.survey_engine.rewards.service;

import com.survey_engine.rewards.dto.LoyaltyAccountResponse;
import com.survey_engine.rewards.models.LoyaltyAccount;
import com.survey_engine.rewards.repository.LoyaltyAccountRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoyaltyAccountService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    @Transactional
    public LoyaltyAccount findOrCreateAccount(String userId) {
        Long tenantId = TenantContext.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> {
                    LoyaltyAccount newAccount = new LoyaltyAccount();
                    newAccount.setTenantId(tenantId);
                    newAccount.setUserId(userId);
                    newAccount.setBalance(BigDecimal.ZERO);
                    return loyaltyAccountRepository.save(newAccount);
                });
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse findAccountByUserId(String userId) {
        Long tenantId = TenantContext.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(this::mapToLoyaltyAccountResponse)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        Long tenantId = TenantContext.getTenantId();
        return loyaltyAccountRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(LoyaltyAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private LoyaltyAccountResponse mapToLoyaltyAccountResponse(LoyaltyAccount account) {
        return new LoyaltyAccountResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
