package com.survey_engine.performance_survey.models.structure;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.structure.enums.OrgRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_org_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrgMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * ID of the User. Soft link to User module.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole role;
}