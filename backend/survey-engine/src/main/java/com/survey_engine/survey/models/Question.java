package com.survey_engine.survey.models;

import com.survey_engine.survey.common.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Lob
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "position", nullable = false)
    private Integer position;

    /** Relative importance weight for scoring (default 1.0). */
    @Column(name = "weight")
    private Double weight = 1.0;

    /**
     * JSON map of option index → score value.
     * Example: {"0": 0, "1": 5, "2": 10, "3": 15}
     * Null means this question is not scored.
     */
    @Column(name = "score_map", columnDefinition = "TEXT")
    private String scoreMap;

    /** Optional category label for grouping scores (e.g. "Customer Service"). */
    @Column(name = "category")
    private String category;

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