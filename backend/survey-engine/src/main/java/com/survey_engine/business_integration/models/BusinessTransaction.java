package com.survey_engine.business_integration.models;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable log of transactions received from business integrations.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "business_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private BusinessIntegration integration;

    @Column(name = "external_transaction_id")
    private String externalTransactionId; // e.g., M-Pesa Receipt Number

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;
}
