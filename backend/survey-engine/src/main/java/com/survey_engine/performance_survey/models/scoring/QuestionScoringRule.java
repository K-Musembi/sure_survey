package com.survey_engine.performance_survey.models.scoring;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.scoring.enums.ScoringStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_question_scoring_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionScoringRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_id", nullable = false)
    private SurveyScoringSchema schema;

    /**
     * Link to the core Question entity.
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Double weight = 1.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_strategy", nullable = false)
    private ScoringStrategy scoringStrategy;

    /**
     * JSON string mapping answer values to scores.
     * e.g., {"Yes": 10, "No": 0, "Maybe": 5}
     */
    @Column(name = "option_score_map", columnDefinition = "TEXT")
    private String optionScoreMap;
}