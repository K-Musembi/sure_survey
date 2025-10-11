package com.survey_engine.rewards.models;

import com.survey_engine.rewards.models.enums.RewardTransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reward_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "participant_id", nullable = false)
    private String participantId;

    @Column(name = "recipient_identifier", nullable = false)
    private String recipientIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RewardTransactionStatus status;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}