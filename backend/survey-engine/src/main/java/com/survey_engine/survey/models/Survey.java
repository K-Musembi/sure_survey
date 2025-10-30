package com.survey_engine.survey.models;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.survey.common.enums.AccessType;
import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.common.enums.SurveyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Survey entity.
 * Maps to database table using JPA Hibernate ORM.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "surveys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "Survey.withQuestions", attributeNodes = @NamedAttributeNode("questions"))
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "introduction")
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SurveyType type;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SurveyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private AccessType accessType;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Response> responses = new ArrayList<>();
}