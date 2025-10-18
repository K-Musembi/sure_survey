package com.survey_engine.payments.models;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.payments.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Model class for the Transaction entity.
 * This entity records the actual, successful financial events related to a payment.
 * It's useful for financial reporting and reconciliation.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

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
}
