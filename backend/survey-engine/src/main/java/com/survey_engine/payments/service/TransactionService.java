package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.TransactionResponse;
import com.survey_engine.payments.models.Transaction;
import com.survey_engine.payments.repository.TransactionRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public TransactionResponse findTransactionById(UUID id) {
        Long tenantId = TenantContext.getTenantId();
        return transactionRepository.findById(id)
                .filter(t -> t.getTenantId().equals(tenantId))
                .map(this::mapToTransactionResponse)
                .orElseThrow(() -> new EntityNotFoundException("Transaction with ID " + id + " not found."));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findTransactionsByPaymentId(UUID paymentId) {
        Long tenantId = TenantContext.getTenantId();
        return transactionRepository.findByPaymentIdAndTenantId(paymentId, tenantId).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

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
