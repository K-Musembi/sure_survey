package com.survey_engine.rewards.models;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.rewards.models.enums.RewardStatus;
import com.survey_engine.rewards.models.enums.RewardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The central entity representing the reward configuration for a survey.
 * It defines the type, value, and rules of the reward campaign.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "survey_id", nullable = false)
    private String surveyId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal total_amount;

    @Column(name = "amount_per_recipient", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPerRecipient;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "reward_provider")
    private String provider;

    @Column(name = "max_recipients")
    private Integer maxRecipients;

    @Column(name = "remaining_rewards")
    private Integer remainingRewards;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RewardStatus status;
}