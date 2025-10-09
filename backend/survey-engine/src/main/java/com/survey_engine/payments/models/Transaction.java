package com.payments.payments.models;

import com.payments.payments.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model class for the Transaction entity.
 * This entity records the actual, successful financial events related to a payment.
 * It's useful for financial reporting and reconciliation.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEvent payment;

    /**
     * The type of transaction: CHARGE, REFUND.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The currency code.
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * The corresponding transaction ID from the gateway.
     */
    @Column(name = "gateway_transaction_id", nullable = false, unique = true)
    private String gatewayTransactionId;

    /**
     * When the transaction was confirmed.
     */
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
