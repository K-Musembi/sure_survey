package com.survey_engine.performance_survey.models.gamification;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_gamification_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GamificationProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "total_points")
    private Long totalPoints = 0L;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "level")
    private Integer level = 1;
}