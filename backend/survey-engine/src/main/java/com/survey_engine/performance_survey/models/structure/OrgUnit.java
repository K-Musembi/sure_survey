package com.survey_engine.performance_survey.models.structure;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.structure.enums.OrgUnitType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_org_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrgUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgUnitType type;

    @Column(name = "parent_id")
    private UUID parentId;

    /**
     * ID of the User who manages this unit.
     * This is a soft link to the User module.
     */
    @Column(name = "manager_id")
    private String managerId;
}