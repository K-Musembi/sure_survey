package com.survey_engine.billing.dto;

import com.survey_engine.billing.models.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for an {@link com.survey_engine.billing.models.Invoice} entity.
 *
 * @param id The unique identifier for the invoice.
 * @param subscriptionId The ID of the subscription this invoice is associated with.
 * @param status The current status of the invoice.
 * @param amount The total amount of the invoice.
 * @param dueDate The date when the payment for this invoice is due.
 * @param paidAt The timestamp when the invoice was successfully paid.
 * @param invoicePdfUrl The URL to the PDF version of the invoice.
 */
public record InvoiceResponse(
        UUID id,
        UUID subscriptionId,
        InvoiceStatus status,
        BigDecimal amount,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        String invoicePdfUrl
) {}
