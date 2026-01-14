package com.survey_engine.performance_survey.models.scoring;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.structure.PerformanceSubject;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The calculated result of a single survey response.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_performance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "response_id", nullable = false, unique = true)
    private Long responseId;

    /**
     * The subject being evaluated.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private PerformanceSubject subject;

    /**
     * The user who filled the survey (could be null for anonymous, or external).
     */
    @Column(name = "evaluator_user_id")
    private String evaluatorUserId;

    @Column(name = "raw_score", nullable = false)
    private Double rawScore;

    /**
     * Normalized score (0-100%).
     */
    @Column(name = "normalized_score", nullable = false)
    private Double normalizedScore;

    /**
     * Snapshot of the organization unit the subject belonged to at the time of recording.
     */
    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
