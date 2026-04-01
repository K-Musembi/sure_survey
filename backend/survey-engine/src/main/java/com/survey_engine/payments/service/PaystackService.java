package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.PaymentEventRequest;
import com.survey_engine.payments.dto.paystack.PaystackRequest;
import com.survey_engine.payments.dto.paystack.PaystackResponse;
import com.survey_engine.payments.dto.paystack.PaystackVerifyResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service class to handle all direct communication with the PayStack API.
 * This class acts as a client for the external PayStack service.
 */
@Service
public class PaystackService {

    // Note: PaystackWebClientConfig.java contains a @Bean that is
    // automatically injected by Spring
    private final WebClient paystackClient;
    private final String frontendBaseUrl;

    public PaystackService(WebClient paystackClient,
                           @org.springframework.beans.factory.annotation.Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.paystackClient = paystackClient;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Initializes a payment transaction with PayStack.
     *
     * @param paymentRequest The payment details from our system.
     * @param userEmail The user's email address.
     * @param reference The unique server-generated reference for this transaction.
     * @return A Mono containing the response from PayStack, including the authorization URL.
     */
    public Mono<PaystackResponse> initializePayment(PaymentEventRequest paymentRequest, String userEmail, String reference, String returnPath) {
        // PayStack expects the amount in the smallest currency unit (e.g., cents for KES, cents for USD).
        long amountInSmallestUnit = paymentRequest.amount().multiply(new BigDecimal(100)).longValue();

        String path = (returnPath != null && !returnPath.isBlank()) ? returnPath : "/dashboard/billing";
        String separator = path.contains("?") ? "&" : "?";
        String callbackUrl = frontendBaseUrl + path + separator + "payment_ref=" + reference;

        PaystackRequest paystackRequest = new PaystackRequest(
                userEmail,
                String.valueOf(amountInSmallestUnit),
                paymentRequest.currency(),
                reference, // Use the server-generated reference
                callbackUrl
        );

        return paystackClient.post()
                .uri("/transaction/initialize")
                .bodyValue(paystackRequest)
                .retrieve()
                .bodyToMono(PaystackResponse.class);
    }

    /**
     * Verifies the status of a transaction with PayStack.
     *
     * @param reference The unique reference ID of the transaction.
     * @return A Mono containing the full transaction details from PayStack.
     */
    public Mono<PaystackVerifyResponse> verifyPayment(String reference) {
        return paystackClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .bodyToMono(PaystackVerifyResponse.class);
    }
}