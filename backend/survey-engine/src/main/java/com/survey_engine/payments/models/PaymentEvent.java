package com.survey_engine.payments.models;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.payments.models.enums.PaymentGateway;
import com.survey_engine.payments.models.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Model class for the Payment entity.
 * This entity tracks each attempt to make a payment, serving as a log for every transaction,
 * whether successful or not.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "survey_id", nullable = false)
    private String surveyId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * The current status: PENDING, PROCESSING, SUCCEEDED, FAILED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    /**
     * The gateway used: PAYSTACK.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", nullable = false)
    private PaymentGateway paymentGateway;

    /**
     * The unique ID for this transaction from the payment gateway (e.g., the 'reference' from PayStack).
     */
    @Column(name = "gateway_transaction_id", unique = true)
    private String gatewayTransactionId;

    /**
     * Stores any error message from the gateway if the payment failed.
     */
    @Column(name = "error_message", length = 255)
    private String errorMessage;
}
