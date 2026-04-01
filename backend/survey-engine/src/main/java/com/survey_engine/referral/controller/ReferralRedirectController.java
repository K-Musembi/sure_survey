package com.survey_engine.referral.controller;

import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.domain.ReferralCode;
import com.survey_engine.referral.repository.ReferralCampaignRepository;
import com.survey_engine.referral.repository.ReferralCodeRepository;
import com.survey_engine.survey.SurveyApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * Public redirect endpoint for referral codes.
 * When a referred user clicks a referral link (/r/{code}), this controller
 * resolves the code → campaign → survey and redirects to the survey URL.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ReferralRedirectController {

    private final ReferralCodeRepository codeRepository;
    private final ReferralCampaignRepository campaignRepository;
    private final SurveyApi surveyApi;

    @Value("${survey.web.base-url}")
    private String webBaseUrl;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String appBaseUrl;

    @GetMapping("/r/{code}")
    public ResponseEntity<Void> handleReferralRedirect(@PathVariable String code) {
        ReferralCode referralCode = codeRepository.findByCode(code).orElse(null);
        if (referralCode == null) {
            log.warn("Invalid referral code: {}", code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(appBaseUrl + "?error=invalid_referral"))
                    .build();
        }

        ReferralCampaign campaign = campaignRepository.findById(referralCode.getCampaignId()).orElse(null);
        if (campaign == null || campaign.getSurveyId() == null) {
            log.warn("Referral code {} has no associated survey", code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(appBaseUrl))
                    .build();
        }

        // Resolve survey URL
        Map<String, Object> survey = surveyApi.getSurveyById(campaign.getSurveyId());
        if (survey == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(appBaseUrl + "?error=survey_not_found"))
                    .build();
        }

        String urlCode = (String) survey.get("urlCode");
        if (urlCode == null || !urlCode.matches("^[a-zA-Z0-9_-]+$")) {
            log.warn("Invalid URL code for survey {}: {}", campaign.getSurveyId(), urlCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(appBaseUrl + "?error=invalid_survey"))
                    .build();
        }

        String surveyUrl = webBaseUrl + urlCode + "?ref=" + code;
        log.info("Referral redirect: code={} → survey={}", code, campaign.getSurveyId());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(surveyUrl))
                .build();
    }
}
