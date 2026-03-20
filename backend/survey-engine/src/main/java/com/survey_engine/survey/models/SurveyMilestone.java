package com.survey_engine.survey.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_milestones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"survey_id", "threshold_pct"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "threshold_pct", nullable = false)
    private int thresholdPct;   // 25 | 50 | 75 | 100

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "badge_type_id")
    private UUID badgeTypeId;   // optional link to ps_badge_definitions

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
