package com.survey_engine.survey.models;

import com.survey_engine.survey.common.enums.ConditionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "branch_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "source_question_id", nullable = false)
    private Long sourceQuestionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    /**
     * JSON string holding condition parameters.
     * ANSWER_EQUALS: {"optionIndex": 2}
     * SCORE_LT / SCORE_GT: {"threshold": 0.6}
     * SCORE_CATEGORY_LT/GT: {"threshold": 0.4, "category": "Compliance"}
     * ALWAYS: null (always redirects to target)
     */
    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String conditionValue;

    /**
     * The question to jump to. Null means end the survey at this branch.
     */
    @Column(name = "target_question_id")
    private Long targetQuestionId;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
