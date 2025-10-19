package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.TransactionResponse;
import com.survey_engine.payments.models.Transaction;
import com.survey_engine.payments.repository.TransactionRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing financial transactions.
 * Provides methods for retrieving transaction details, scoped by tenant.
 */
@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserApi userApi;

    /**
     * Finds a single transaction by its ID, scoped by the current tenant.
     *
     * @param id The UUID of the transaction.
     * @return A {@link TransactionResponse} DTO representing the found transaction.
     * @throws EntityNotFoundException if the transaction is not found for the given ID and tenant.
     */
    @Transactional(readOnly = true)
    public TransactionResponse findTransactionById(UUID id) {
        Long tenantId = userApi.getTenantId();
        return transactionRepository.findById(id)
                .filter(t -> t.getTenantId().equals(tenantId))
                .map(this::mapToTransactionResponse)
                .orElseThrow(() -> new EntityNotFoundException("Transaction with ID " + id + " not found."));
    }

    /**
     * Finds all transactions associated with a specific payment ID, scoped by the current tenant.
     *
     * @param paymentId The UUID of the parent payment event.
     * @return A list of {@link TransactionResponse} DTOs for the specified payment and tenant.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findTransactionsByPaymentId(UUID paymentId) {
        Long tenantId = userApi.getTenantId();
        return transactionRepository.findByPaymentIdAndTenantId(paymentId, tenantId).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a {@link Transaction} entity to a {@link TransactionResponse} DTO.
     *
     * @param transaction The {@link Transaction} entity to map.
     * @return The corresponding {@link TransactionResponse} DTO.
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getPayment().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getGatewayTransactionId(),
                transaction.getCreatedAt()
        );
    }
}
