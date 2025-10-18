package com.survey_engine.user.repository;

import com.survey_engine.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByTenantId(Long tenantId);

/**
 * Finds all User entities belonging to a specific tenant.
 *
 * @param tenantId The ID of the tenant.
 * @return A list of User entities for the given tenant.
 */
}

//findById(), findAll(), etc. provided by default through JpaRepository
