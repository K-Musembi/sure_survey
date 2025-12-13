package com.survey_engine.survey.models;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class for DistributionList entity
 * Maps to database table using JPA Hibernate ORM
 */
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "distribution_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistributionList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "distributionList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistributionListContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "distributionList", cascade = CascadeType.ALL)
    private List<Survey> surveys = new ArrayList<>();
}
