package com.survey_engine.business_integration.service;

import com.survey_engine.business_integration.dto.BusinessTransactionResponse;
import com.survey_engine.business_integration.models.BusinessTransaction;
import com.survey_engine.business_integration.repository.BusinessTransactionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing business transactions.
 */
@Service
@RequiredArgsConstructor
public class BusinessTransactionService {

    private final BusinessTransactionRepository transactionRepository;
    private final UserApi userApi;

    /**
     * Retrieves all transactions associated with a specific integration.
     * Ensures the integration belongs to the authenticated user's tenant.
     *
     * @param integrationId The ID of the business integration.
     * @return A list of {@link BusinessTransactionResponse} DTOs.
     * @throws EntityNotFoundException if no transactions are found or access is denied (implicitly via tenant check).
     */
    @Transactional(readOnly = true)
    public List<BusinessTransactionResponse> getTransactionsByIntegrationId(UUID integrationId) {
        Long tenantId = userApi.getTenantId();
        List<BusinessTransaction> transactions = transactionRepository.findByIntegrationIdAndTenantId(integrationId, tenantId);
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a {@link BusinessTransaction} entity to a {@link BusinessTransactionResponse} DTO.
     *
     * @param transaction The entity to map.
     * @return The response DTO.
     */
    private BusinessTransactionResponse mapToResponse(BusinessTransaction transaction) {
        return new BusinessTransactionResponse(
                transaction.getId(),
                transaction.getIntegration().getId(),
                transaction.getExternalTransactionId(),
                transaction.getMsisdn(),
                transaction.getFirstName(),
                transaction.getLastName(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getCreatedAt()
        );
    }
}
