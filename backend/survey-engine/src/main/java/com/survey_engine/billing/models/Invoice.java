package com.survey_engine.billing.models;

import com.survey_engine.billing.models.enums.InvoiceStatus;
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
 * Represents a record of a bill for a tenant.
 * This entity stores details about each invoice generated for a subscription.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "billing_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseEntity {

    /**
     * The unique identifier for the invoice.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * The subscription this invoice is associated with.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    /**
     * The ID of the user this invoice belongs to.
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * The unique identifier for this invoice in the Paystack payment gateway.
     */
    @Column(unique = true)
    private String paystackInvoiceId;

    /**
     * The current status of the invoice (e.g., DRAFT, OPEN, PAID, FAILED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    /**
     * The total amount of the invoice.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The date when the payment for this invoice is due.
     */
    private LocalDateTime dueDate;

    /**
     * The timestamp when the invoice was successfully paid.
     */
    private LocalDateTime paidAt;

    /**
     * The URL to the PDF version of the invoice, as provided by the payment gateway.
     */
    private String invoicePdfUrl;
}