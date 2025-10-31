package com.survey_engine.billing.service;

import com.survey_engine.billing.dto.paystack.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service for interacting with Paystack's subscription-related APIs.
 * This service handles creating customers, creating subscriptions, and canceling subscriptions
 * directly with Paystack.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackSubscriptionService {

    private final WebClient paystackBillingClient;

    /**
     * Creates a customer in Paystack.
     *
     * @param email The customer's email address.
     * @param firstName The first name of the customer.
     * @param lastName The last name of the customer.
     * @param phone The phone number of the customer.
     * @return The Paystack customer code.
     */
    public String createCustomer(String email, String firstName, String lastName, String phone) {
        log.info("Creating Paystack customer for email: {}", email);
        PaystackCustomerRequest request = new PaystackCustomerRequest(email, firstName, lastName, phone);

        PaystackCustomerResponse response = paystackBillingClient.post()
                .uri("/customer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackCustomerResponse.class)
                .block(); // Blocking for simplicity, consider reactive approach

        if (response != null && response.status() && response.data() != null) {
            log.info("Paystack customer created with code: {}", response.data().customerCode());
            return response.data().customerCode();
        } else {
            log.error("Failed to create Paystack customer: {}", response != null ? response.message() : "No response");
            throw new RuntimeException("Failed to create Paystack customer.");
        }
    }

    /**
     * Creates a subscription in Paystack.
     *
     * @param customerCode The Paystack customer code.
     * @param planCode The Paystack plan code.
     * @return The Paystack subscription code.
     */
    public String createSubscription(String customerCode, String planCode) {
        log.info("Creating Paystack subscription for customer: {} and plan: {}", customerCode, planCode);
        PaystackSubscriptionRequest request = new PaystackSubscriptionRequest(customerCode, planCode, null);

        PaystackSubscriptionResponse response = paystackBillingClient.post()
                .uri("/subscription")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackSubscriptionResponse.class)
                .block(); // Blocking for simplicity, consider reactive approach

        if (response != null && response.status() && response.data() != null) {
            log.info("Paystack subscription created with code: {}", response.data().subscriptionCode());
            return response.data().subscriptionCode();
        } else {
            log.error("Failed to create Paystack subscription: {}", response != null ? response.message() : "No response");
            throw new RuntimeException("Failed to create Paystack subscription.");
        }
    }

    /**
     * Cancels a subscription in Paystack.
     *
     * @param subscriptionCode The Paystack subscription code.
     * @param emailToken The email token for confirmation (if required by Paystack).
     * @return True if cancellation was successful, false otherwise.
     */
    public boolean cancelSubscription(String subscriptionCode, String emailToken) {
        log.info("Cancelling Paystack subscription: {}", subscriptionCode);
        PaystackDisableSubscriptionRequest request = new PaystackDisableSubscriptionRequest(subscriptionCode, emailToken);

        PaystackDisableSubscriptionResponse response = paystackBillingClient.post()
                .uri("/subscription/disable")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackDisableSubscriptionResponse.class)
                .block(); // Blocking for simplicity, consider reactive approach

        if (response != null && response.status()) {
            log.info("Paystack subscription {} cancelled successfully.", subscriptionCode);
            return true;
        } else {
            log.error("Failed to cancel Paystack subscription {}: {}", subscriptionCode, response != null ? response.message() : "No response");
            return false;
        }
    }
}

