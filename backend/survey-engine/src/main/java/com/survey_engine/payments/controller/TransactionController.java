package com.survey_engine.payments.controller;

import com.survey_engine.payments.dto.TransactionResponse;
import com.survey_engine.payments.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for retrieving transaction history.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@AllArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Get a single transaction by its internal UUID.
     *
     * @param id The UUID of the transaction.
     * @return A ResponseEntity containing the transaction details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        TransactionResponse transaction = transactionService.findTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Get all transactions associated with a single payment.
     *
     * @param paymentId The UUID of the parent payment.
     * @return A ResponseEntity containing a list of transaction details.
     */
    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPaymentId(@PathVariable UUID paymentId) {
        List<TransactionResponse> transactions = transactionService.findTransactionsByPaymentId(paymentId);
        return ResponseEntity.ok(transactions);
    }
}