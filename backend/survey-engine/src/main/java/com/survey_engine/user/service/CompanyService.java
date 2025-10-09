package com.survey_engine.user.service;

import com.survey_engine.user.dto.CompanyRequest;
import com.survey_engine.user.dto.CompanyResponse;
import com.survey_engine.user.models.Company;
import com.survey_engine.user.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Company entity
 * Defines business logic
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * Constructor method
     * @param companyRepository - company repository instance
     */
    @Autowired
    public CompanyService(CompanyRepository companyRepository) {

        this.companyRepository = companyRepository;
    }

    /**
     * Method to create a new company
     * @param companyRequest - request DTO
     * @return - response DTO
     */
    @Transactional
    public CompanyResponse createCompany(CompanyRequest companyRequest) {
        if (companyRepository.findByName(companyRequest.name()).isPresent()) {
            throw new DataIntegrityViolationException("Company already exists");
        }

        Company company = new Company();
        Company savedCompany = getCompany(company, companyRequest);
        return mapToCompanyResponse(savedCompany);
    }

    /**
     * Method to find company by id
     * @param id - company id
     * @return - response DTO
     */
    @Transactional
    public CompanyResponse findCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        return mapToCompanyResponse(company);
    }

    /**
     * Method to find company by email
     * @param name - company email
     * @return - response DTO
     */
    @Transactional
    public CompanyResponse findCompanyByName(String name) {
        Company company =  companyRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        return mapToCompanyResponse(company);
    }

    @Transactional
    public List<CompanyResponse> findAllCompanies() {
        List<Company> companies = companyRepository.findAll();

        return companies.stream()
                .map(this::mapToCompanyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to update company properties
     * @param id - company id
     * @param companyRequest - request DTO
     * @return - response DTO
     */
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest companyRequest) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Company savedCompany = getCompany(company, companyRequest);
        return mapToCompanyResponse(savedCompany);
    }

    /**
     * Method to delete company
     * @param id - company id
     */
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        companyRepository.delete(company);
    }

    /**
     * Method to retrieve company properties and save company in database
     * @param company - company instance
     * @param companyRequest - request DTO
     * @return - saved company
     */
    private Company getCompany(Company company, CompanyRequest companyRequest) {
        company.setName(companyRequest.name());
        company.setSector(companyRequest.sector());
        company.setCountry(companyRequest.country());

        return companyRepository.save(company);
    }

    /**
     * Method to map company to response DTO
     * @param company - company instance
     * @return - response DTO
     */
    private CompanyResponse mapToCompanyResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getSector(),
                company.getCountry()
        );
    }
}