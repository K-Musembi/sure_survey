package com.survey_engine.performance_survey.service.listener;

import com.survey_engine.performance_survey.events.ScoreCalculatedEvent;
import com.survey_engine.performance_survey.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationEventListener {

    private final GamificationService gamificationService;

    @Async
    @EventListener
    public void onScoreCalculated(ScoreCalculatedEvent event) {
        if (event.subjectUserId() == null) {
            return;
        }
        log.info(" awarding gamification points to {}", event.subjectUserId());
        
        // Simple Rule: 10 points for completion + 1 point per normalized score
        long points = 10 + Math.round(event.normalizedScore());
        gamificationService.awardPoints(event.subjectUserId(), points);
    }
}
