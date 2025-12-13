package com.survey_engine.survey.controller;

import com.survey_engine.survey.dto.ContactRequest;
import com.survey_engine.survey.dto.DistributionListRequest;
import com.survey_engine.survey.dto.DistributionListResponse;
import com.survey_engine.survey.service.DistributionListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/distribution-lists")
@RequiredArgsConstructor
@Validated
public class DistributionListController {

    private final DistributionListService distributionListService;

    /**
     * Creates a new distribution list for the authenticated user via JSON.
     * @param request The request body containing the list name and contacts.
     * @param jwt The authenticated user's JWT.
     * @return A ResponseEntity containing the created distribution list response.
     */
    @PostMapping
    public ResponseEntity<DistributionListResponse> createDistributionList(
            @Valid @RequestBody DistributionListRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED).body(distributionListService.createDistributionList(userId, request));
    }

    /**
     * Creates a new distribution list by uploading a CSV file.
     * @param name The name of the distribution list.
     * @param file The CSV file containing contacts (format: phone, firstName, lastName, email).
     * @param jwt The authenticated user's JWT.
     * @return A ResponseEntity containing the created distribution list response.
     */
    @PostMapping(value = "/upload-csv", consumes = "multipart/form-data")
    public ResponseEntity<DistributionListResponse> uploadCsv(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(distributionListService.createDistributionListFromCsv(userId, name, file));
    }

    /**
     * Retrieves all distribution lists owned by the authenticated user.
     * @param jwt The authenticated user's JWT.
     * @return A ResponseEntity containing a list of distribution list responses.
     */
    @GetMapping
    public ResponseEntity<List<DistributionListResponse>> getAllDistributionLists(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(distributionListService.getAllDistributionLists(userId));
    }

    /**
     * Retrieves a specific distribution list by its ID, owned by the authenticated user.
     * @param id The ID of the distribution list.
     * @param jwt The authenticated user's JWT.
     * @return A ResponseEntity containing the distribution list response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DistributionListResponse> getDistributionListById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(distributionListService.getDistributionListById(userId, id));
    }

    /**
     * Adds contacts to an existing distribution list owned by the authenticated user.
     * @param id The ID of the distribution list.
     * @param contactRequests A list of contacts to add.
     * @param jwt The authenticated user's JWT.
     * @return A ResponseEntity containing the updated distribution list response.
     */
    @PostMapping("/{id}/contacts")
    public ResponseEntity<DistributionListResponse> addContacts(
            @PathVariable UUID id,
            @RequestBody List<ContactRequest> contactRequests,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(distributionListService.addContacts(userId, id, contactRequests));
    }
}
