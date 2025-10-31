package com.survey_engine.billing.models.enums;

/**
 * Enum representing the status of a tenant's subscription.
 */
public enum SubscriptionStatus {
    /**
     * The subscription is active and the tenant has access to the features.
     */
    ACTIVE,

    /**
     * The subscription has been canceled and will not renew.
     */
    CANCELED,

    /**
     * The payment for the latest billing period has failed.
     */
    PAST_DUE,

    /**
     * The subscription is in a trial period.
     */
    TRIALING
}