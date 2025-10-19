package com.survey_engine.user.controller;

import com.survey_engine.user.dto.UserRequest;
import com.survey_engine.user.dto.UserResponse;
import com.survey_engine.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for user entity
 * HTTP requests and responses
 */
@RestController
@Validated
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * Method to retrieve user by id
     * @param id - user id
     * @return - HTTP response
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse responseObject = userService.findUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve user by email
     * @param email - user email
     * @return - HTTP response
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse responseObject = userService.findUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve users by tenant id
     * @param tenantId - tenant id
     * @return - HTTP response
     */
    @GetMapping("/tenant")
    public ResponseEntity<List<UserResponse>> getUsersByTenantId(@RequestParam Long tenantId) {
        List<UserResponse> responseObject = userService.findUsersByTenantId(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to update user
     * @param id - user id
     * @param userRequest - request DTO containing user details, including tenantId
     * @return - HTTP response
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        UserResponse responseObject = userService.updateUser(id, userRequest);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to delete user
     * @param id - user id
     * @return - HTTP response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
