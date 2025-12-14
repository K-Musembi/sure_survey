package com.survey_engine.business_integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Payload sent by Daraja to the Confirmation URL.
 */
public record DarajaConfirmationRequest(
        @JsonProperty("TransactionType") String transactionType,
        @JsonProperty("TransID") String transId,
        @JsonProperty("TransTime") String transTime,
        @JsonProperty("TransAmount") String transAmount,
        @JsonProperty("BusinessShortCode") String businessShortCode,
        @JsonProperty("BillRefNumber") String billRefNumber,
        @JsonProperty("InvoiceNumber") String invoiceNumber,
        @JsonProperty("OrgAccountBalance") String orgAccountBalance,
        @JsonProperty("ThirdPartyTransID") String thirdPartyTransID,
        @JsonProperty("MSISDN") String msisdn,
        @JsonProperty("FirstName") String firstName,
        @JsonProperty("MiddleName") String middleName,
        @JsonProperty("LastName") String lastName
) {}
