package com.survey_engine.performance_survey.models.scoring;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Configuration for a specific survey's scoring rules.
 * If this exists for a surveyId, the survey is considered a "Performance Survey".
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_survey_scoring_schemas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyScoringSchema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Link to the core Survey entity.
     */
    @Column(name = "survey_id", nullable = false, unique = true)
    private Long surveyId;

    @Column(name = "default_question_weight")
    private Double defaultQuestionWeight = 1.0;

    @Column(name = "target_score")
    private Double targetScore = 100.0;
}