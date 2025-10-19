package com.survey_engine.user.controller;

import com.survey_engine.user.dto.ParticipantRequest;
import com.survey_engine.user.dto.ParticipantResponse;
import com.survey_engine.user.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing participant-related operations.
 */
@RestController
@RequestMapping("/api/v1/participants")
@RequiredArgsConstructor
@Validated
public class ParticipantController {

    private final ParticipantService participantService;

    /**
     * Creates a new participant.
     *
     * @param request The request body containing participant details.
     * @return A {@link ResponseEntity} containing the created participant's response and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<ParticipantResponse> createParticipant(
            @Valid @RequestBody ParticipantRequest request) {
        ParticipantResponse response = participantService.createParticipant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a participant by their ID.
     *
     * @param id The ID of the participant to retrieve.
     * @return A {@link ResponseEntity} containing the participant's response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParticipantResponse> getParticipantById(@PathVariable Long id) {
        ParticipantResponse response = participantService.findParticipantById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a participant by their ID.
     *
     * @param id The ID of the participant to delete.
     * @return A {@link ResponseEntity} with no content and HTTP status 204.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }
}
