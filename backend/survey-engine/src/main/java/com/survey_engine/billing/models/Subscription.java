package com.survey_engine.billing.models;

import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an instance of a tenant subscribing to a specific plan.
 * This entity links a tenant to a plan and tracks the lifecycle of their subscription.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "billing_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    /**
     * The unique identifier for the subscription.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * The plan to which the tenant is subscribed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * The unique identifier for this subscription in the Paystack payment gateway.
     */
    @Column(unique = true)
    private String paystackSubscriptionId;

    /**
     * The current status of the subscription (e.g., ACTIVE, CANCELED, PAST_DUE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /**
     * The start of the current billing period.
     */
    private LocalDateTime currentPeriodStart;

    /**
     * The end of the current billing period.
     */
    private LocalDateTime currentPeriodEnd;

    /**
     * The end date of the trial period, if applicable.
     */
    private LocalDateTime trialEndDate;

    /**
     * The email token for this subscription in the Paystack payment gateway, used for actions like cancellation.
     */
    private String paystackEmailToken;
}