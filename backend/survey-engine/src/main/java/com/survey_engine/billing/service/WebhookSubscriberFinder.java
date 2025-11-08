package com.survey_engine.billing.service;

import com.survey_engine.billing.dto.SubscriberInfo;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A helper component to find subscriber information (tenant and user) from webhook event data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookSubscriberFinder {

    private final UserApi userApi;

    /**
     * Extracts the customer email from the webhook event data and finds the corresponding tenant and user IDs.
     *
     * @param eventData The data payload of the event.
     * @return A {@link SubscriberInfo} object containing the tenantId and userId.
     * @throws EntityNotFoundException if the user is not found for the given email.
     */
    @SuppressWarnings("unchecked")
    public SubscriberInfo findSubscriber(Map<String, Object> eventData) {
        Object customerObject = eventData.get("customer");
        if (!(customerObject instanceof Map)) {
            log.error("Customer data is not a Map in webhook payload: {}", eventData);
            throw new IllegalArgumentException("Invalid customer data in webhook payload.");
        }
        Map<String, Object> customerData = (Map<String, Object>) customerObject;
        String email = (String) customerData.get("email");

        if (email == null) {
            log.error("Email is null in customer data: {}", customerData);
            throw new IllegalArgumentException("Customer email is missing in webhook payload.");
        }

        Long tenantId = userApi.findTenantIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found for email: " + email));

        Long userId = userApi.findUserIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));

        return new SubscriberInfo(tenantId, userId);
    }
}
