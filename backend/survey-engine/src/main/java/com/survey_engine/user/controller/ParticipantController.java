package com.survey_engine.user.controller;

import com.survey_engine.user.dto.ParticipantRequest;
import com.survey_engine.user.dto.ParticipantResponse;
import com.survey_engine.user.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participants")
@RequiredArgsConstructor
@Validated
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping
    public ResponseEntity<ParticipantResponse> createParticipant(
            @Valid @RequestBody ParticipantRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String authUserId = jwt.getSubject();
        ParticipantResponse response = participantService.createParticipant(request, authUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantResponse> getParticipantById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String authUserId = jwt.getSubject();
        ParticipantResponse response = participantService.findParticipantById(id, authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ParticipantResponse>> getParticipantsByCompany(
            @PathVariable Long companyId,
            @AuthenticationPrincipal Jwt jwt) {
        String authUserId = jwt.getSubject();
        List<ParticipantResponse> responses = participantService.findParticipantsByCompany(companyId, authUserId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipantResponse> updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody ParticipantRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String authUserId = jwt.getSubject();
        ParticipantResponse response = participantService.updateParticipant(id, request, authUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String authUserId = jwt.getSubject();
        participantService.deleteParticipant(id, authUserId);
        return ResponseEntity.noContent().build();
    }
}