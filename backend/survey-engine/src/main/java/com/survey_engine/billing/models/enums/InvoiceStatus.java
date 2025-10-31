package com.survey_engine.billing.models.enums;

/**
 * Enum representing the status of an invoice.
 */
public enum InvoiceStatus {
    /**
     * The invoice has been created but is not yet finalized.
     */
    DRAFT,

    /**
     * The invoice is finalized and awaiting payment.
     */
    OPEN,

    /**
     * The invoice has been successfully paid.
     */
    PAID,

    /**
     * The payment for the invoice has failed.
     */
    FAILED,

    /**
     * The invoice has been voided and is no longer payable.
     */
    VOID
}