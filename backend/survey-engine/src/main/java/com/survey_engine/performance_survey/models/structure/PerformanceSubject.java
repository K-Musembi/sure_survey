package com.survey_engine.performance_survey.models.structure;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.structure.enums.OrgRole;
import com.survey_engine.performance_survey.models.structure.enums.SubjectType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_performance_subjects", indexes = {
    @Index(name = "idx_ps_subject_ref", columnList = "reference_code, tenant_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSubject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * ID of the User. Nullable for non-user subjects.
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * Unique reference code within the tenant (e.g., Employee ID, Store Code).
     */
    @Column(name = "reference_code", nullable = false)
    private String referenceCode;

    /**
     * Display name for the subject (e.g., Username or Unit Name).
     */
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole role;
}
