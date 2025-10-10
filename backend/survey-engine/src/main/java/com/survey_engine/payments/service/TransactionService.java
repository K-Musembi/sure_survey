package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.TransactionResponse;
import com.survey_engine.payments.models.Transaction;
import com.survey_engine.payments.repository.TransactionRepository;
import com.survey_engine.payments.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for retrieving completed transaction data.
 */
@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Finds a single transaction by its unique ID.
     *
     * @param id The UUID of the transaction.
     * @return A DTO representing the transaction.
     */
    @Transactional(readOnly = true)
    public TransactionResponse findTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .map(this::mapToTransactionResponse)
                .orElseThrow(() -> new EntityNotFoundException("Transaction with ID " + id + " not found."));
    }

    /**
     * Finds all transactions associated with a specific payment ID.
     *
     * @param paymentId The UUID of the parent payment.
     * @return A list of transaction DTOs.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findTransactionsByPaymentId(UUID paymentId) {
        return transactionRepository.findByPaymentId(paymentId).stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Transaction entity to its corresponding DTO.
     *
     * @param transaction The entity to map.
     * @return The mapped DTO.
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getPayment().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getGatewayTransactionId(),
                transaction.getProcessedAt()
        );
    }
}