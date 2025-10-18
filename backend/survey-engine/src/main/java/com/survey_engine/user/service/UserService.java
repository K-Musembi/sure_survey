package com.survey_engine.user.service;

import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.dto.UserRequest;
import com.survey_engine.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for User entity
 * Defines business logic
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor method
     * @param userRepository - user repository instance
     * @param tenantRepository - tenant repository instance
     * @param passwordEncoder - password encoder instance
     */
    @Autowired
    public UserService(UserRepository userRepository, TenantRepository tenantRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Method to find user by id, scoped by the current tenant.
     * @param id - user id
     * @return - response DTO
     * @throws EntityNotFoundException if the user is not found within the current tenant.
     */
    @Transactional
    public UserResponse findUserById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    /**
     * Method to find user by email, scoped by the current tenant.
     * @param email - user email
     * @return - response DTO
     * @throws EntityNotFoundException if the user is not found within the current tenant.
     */
    @Transactional
    public UserResponse findUserByEmail(String email) {
        Long tenantId = TenantContext.getTenantId();
        User user =  userRepository.findByEmail(email)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    /**
     * Method to find users by tenant id.
     * @param tenantId - tenant id
     * @return - List of response DTOs
     */
    @Transactional
    public List<UserResponse> findUsersByTenantId(Long tenantId) {
        List<User> users = userRepository.findByTenantId(tenantId);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to update user properties, scoped by the current tenant.
     * @param id - user id
     * @param userRequest - request DTO
     * @return - response DTO
     * @throws EntityNotFoundException if the user is not found within the current tenant.
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        Long tenantId = TenantContext.getTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User savedUser = getUser(user, userRequest);
        return mapToUserResponse(savedUser);
    }

    /**
     * Method to delete user, scoped by the current tenant.
     * @param id - user id
     * @throws EntityNotFoundException if the user is not found within the current tenant.
     */
    @Transactional
    public void deleteUser(Long id) {
        Long tenantId = TenantContext.getTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Method to retrieve user properties and save user in database.
     * This method handles setting the tenant for the user based on the UserRequest.
     * @param user - user instance
     * @param userRequest - request DTO
     * @return - saved user
     * @throws EntityNotFoundException if the specified tenant is not found.
     */
    private User getUser(User user, UserRequest userRequest) {
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        if (userRequest.role() != null) {
            user.setRole(userRequest.role());
        }
        if (userRequest.tenantId() != null) {
            Tenant tenant = tenantRepository.findById(userRequest.tenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Tenant Not Found"));
            user.setTenant(tenant);
            user.setTenantId(tenant.getId()); // Set tenantId in BaseEntity
        }

        return userRepository.save(user);
    }

    /**
     * Method to map user to response DTO.
     * @param user - user instance
     * @return - response DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTenantId()
        );
    }
}