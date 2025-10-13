package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.RewardRequest;
import com.survey_engine.rewards.dto.RewardResponse;
import com.survey_engine.rewards.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing Reward configurations.
 * Provides endpoints for creating, retrieving, and cancelling rewards.
 */

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * Creates a new reward configuration for a survey.
     * The user must be authenticated.
     *
     * @param rewardRequest The request body containing reward details.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the created reward and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<RewardResponse> createReward(@Valid @RequestBody RewardRequest rewardRequest, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        RewardResponse createdReward = rewardService.createReward(rewardRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReward);
    }

    /**
     * Retrieves the reward configuration for a specific survey.
     *
     * @param surveyId The ID of the survey.
     * @return A ResponseEntity containing the reward details.
     */
    @GetMapping("/survey/{surveyId}")
    public ResponseEntity<RewardResponse> getRewardBySurveyId(@PathVariable String surveyId) {
        return ResponseEntity.ok(rewardService.findRewardBySurveyId(surveyId));
    }

    /**
     * Retrieves all reward configurations created by the authenticated user.
     *
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing a list of the user's rewards.
     */
    @GetMapping("/my-rewards")
    public ResponseEntity<List<RewardResponse>> getMyRewards(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(rewardService.findRewardsByUserId(userId));
    }

    /**
     * Cancels a reward campaign.
     *
     * @param rewardId The ID of the reward to cancel.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity containing the updated reward details.
     */
    @PostMapping("/{rewardId}/cancel")
    public ResponseEntity<RewardResponse> cancelReward(@PathVariable UUID rewardId, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(rewardService.cancelReward(rewardId, userId));
    }
}
