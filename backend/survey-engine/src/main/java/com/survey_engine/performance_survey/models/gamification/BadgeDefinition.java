package com.survey_engine.performance_survey.models.gamification;

import com.survey_engine.common.models.BaseEntity;
import com.survey_engine.performance_survey.models.gamification.enums.BadgeTriggerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_badge_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private BadgeTriggerType triggerType;

    @Column(nullable = false)
    private Double threshold;
}