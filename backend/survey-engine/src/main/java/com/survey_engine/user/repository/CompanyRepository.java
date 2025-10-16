package com.survey_engine.user.repository;

import com.survey_engine.user.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Company entity.
 * Provides CRUD operations and custom queries for companies.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);
}

//findById(), findAll(), etc. provided by default through JpaRepository