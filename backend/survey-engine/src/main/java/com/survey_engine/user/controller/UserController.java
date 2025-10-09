package com.survey_engine.user.controller;

import com.user_service.user_service.user.dto.UserRequest;
import com.user_service.user_service.user.dto.UserResponse;
import jakarta.validation.Valid;
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
public class UserController {

    private final com.user_service.user_service.user.UserService userService;

    /**
     * Constructor for user controller class
     * @param userService - user service instance
     */
    @Autowired
    public UserController(com.user_service.user_service.user.UserService userService) {
        this.userService = userService;
    }


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
     * Method to retrieve users by company id
     * @param companyId - company id
     * @return - HTTP response
     */
    @GetMapping("/company")
    public ResponseEntity<List<UserResponse>> getUsersByCompanyId(@RequestParam Long companyId) {
        List<UserResponse> responseObject = userService.findUsersByCompanyId(companyId);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to update user
     * @param id - user id
     * @param userRequest - request DTO
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
