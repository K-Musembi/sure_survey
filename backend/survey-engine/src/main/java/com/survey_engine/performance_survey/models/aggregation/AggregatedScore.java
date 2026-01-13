package com.survey_engine.performance_survey.models.aggregation;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.aggregation.enums.AggregationPeriod;
import com.survey_engine.performance_survey.models.structure.OrgUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pre-calculated aggregated scores for efficient dashboard loading.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_aggregated_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedScore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AggregationPeriod period;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Double score;

    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;
}