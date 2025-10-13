package com.survey_engine.rewards.controller;

import com.survey_engine.rewards.dto.RewardRedemptionRequest;
import com.survey_engine.rewards.service.RewardRedemptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling reward redemption claims.
 */
@RestController
@RequestMapping("/api/v1/rewards/redemption")
@RequiredArgsConstructor
public class RewardRedemptionController {

    private final RewardRedemptionService rewardRedemptionService;

    /**
     * Endpoint for an authenticated user to claim a reward after completing a survey.
     * This is typically used for web-based surveys where a phone number is required
     * to deliver a reward like airtime.
     *
     * @param request The request body containing the survey ID and phone number.
     * @param jwt The JWT of the authenticated user.
     * @return A ResponseEntity indicating the request has been accepted for processing.
     */
    @PostMapping("/claim")
    public ResponseEntity<Void> claimReward(
            @Valid @RequestBody RewardRedemptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        rewardRedemptionService.claimReward(request.surveyId(), request.phoneNumber(), userId);
        
        // Return 202 Accepted as the fulfillment is an asynchronous process.
        return ResponseEntity.accepted().build();
    }
}