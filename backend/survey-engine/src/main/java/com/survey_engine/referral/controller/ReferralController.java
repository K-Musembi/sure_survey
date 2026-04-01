package com.survey_engine.referral.controller;

import com.survey_engine.referral.domain.DataSubjectRequest;
import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.domain.enums.CampaignStatus;
import com.survey_engine.referral.domain.enums.RequestStatus;
import com.survey_engine.referral.dto.CampaignRequest;
import com.survey_engine.referral.dto.DataSubjectAccessResponse;
import com.survey_engine.referral.dto.DataSubjectRequestBody;
import com.survey_engine.referral.dto.SendInviteRequest;
import com.survey_engine.referral.dto.SendInviteResult;
import com.survey_engine.referral.service.DataSubjectService;
import com.survey_engine.referral.service.ReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;
    private final DataSubjectService dataSubjectService;

    @PostMapping("/campaigns")
    public ResponseEntity<ReferralCampaign> createCampaign(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CampaignRequest request) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(referralService.createCampaign(tenantId, request));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<ReferralCampaign>> getCampaigns(@AuthenticationPrincipal Jwt jwt) {
        Long tenantId = extractTenantId(jwt);
        return ResponseEntity.ok(referralService.getCampaignsForTenant(tenantId));
    }

    @PatchMapping("/campaigns/{campaignId}/status")
    public ResponseEntity<Void> updateCampaignStatus(
            @PathVariable UUID campaignId,
            @RequestParam CampaignStatus status) {
        referralService.updateCampaignStatus(campaignId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the campaign's stated purpose and increments the consent version.
     * Existing consents are now considered stale — affected participants must re-consent.
     */
    @PatchMapping("/campaigns/{campaignId}/purpose")
    public ResponseEntity<ReferralCampaign> updateCampaignPurpose(
            @PathVariable UUID campaignId,
            @RequestBody Map<String, String> body) {
        String newPurpose = body.get("purposeDescription");
        return ResponseEntity.ok(referralService.updateCampaignPurpose(campaignId, newPurpose));
    }

    @PostMapping("/invites")
    public ResponseEntity<SendInviteResult> sendInvite(@Valid @RequestBody SendInviteRequest request) {
        SendInviteResult result = referralService.sendInvite(request);
        HttpStatus status = result.success() ? HttpStatus.CREATED : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Webhook/callback for SMS opt-in reply (YES).
     * Called by the SMS gateway or USSD session handler.
     */
    @PostMapping("/invites/{inviteId}/opt-in")
    public ResponseEntity<Void> optIn(@PathVariable UUID inviteId) {
        referralService.processOptIn(inviteId);
        return ResponseEntity.ok().build();
    }

    /**
     * Webhook/callback for SMS opt-out reply (STOP).
     */
    @PostMapping("/invites/{inviteId}/opt-out")
    public ResponseEntity<Void> optOut(@PathVariable UUID inviteId) {
        referralService.processOptOut(inviteId);
        return ResponseEntity.ok().build();
    }

    /**
     * Called when the referred person completes the required action
     * (survey completion or service signup).
     */
    @PostMapping("/invites/{inviteId}/complete")
    public ResponseEntity<Void> markActionCompleted(@PathVariable UUID inviteId) {
        referralService.onActionCompleted(inviteId);
        return ResponseEntity.ok().build();
    }

    /**
     * Subject Access Request: returns all referral and consent data held for a phone number.
     * Rate-limited to 3 requests per phone per 24 hours.
     */
    @PostMapping("/subjects/access")
    public ResponseEntity<DataSubjectAccessResponse> subjectAccessRequest(
            @Valid @RequestBody DataSubjectRequestBody body) {
        return ResponseEntity.ok(dataSubjectService.processAccessRequest(body.phone()));
    }

    /**
     * Erasure Request: pseudonymises all referral data held for a phone number.
     * Rate-limited to 3 requests per phone per 24 hours.
     */
    @PostMapping("/subjects/erasure")
    public ResponseEntity<Map<String, String>> subjectErasureRequest(
            @Valid @RequestBody DataSubjectRequestBody body) {
        UUID dsrId = dataSubjectService.processErasureRequest(body.phone());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Erasure request processed. All referral data has been pseudonymised.",
                "requestId", dsrId.toString()
        ));
    }

    @GetMapping("/subjects/admin/requests")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<DataSubjectRequest>> listDataSubjectRequests() {
        return ResponseEntity.ok(dataSubjectService.getAllRequests());
    }

    @PatchMapping("/subjects/admin/requests/{dsrId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DataSubjectRequest> updateDataSubjectRequest(
            @PathVariable UUID dsrId,
            @RequestBody Map<String, String> body) {
        RequestStatus status = RequestStatus.valueOf(body.get("status"));
        String notes = body.get("notes");
        return ResponseEntity.ok(dataSubjectService.updateRequestStatus(dsrId, status, notes));
    }

    private Long extractTenantId(Jwt jwt) {
        return jwt.getClaim("tenantId");
    }
}
