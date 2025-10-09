package com.survey_engine.user.company;

import com.survey_engine.user.company.dto.CompanyRequest;
import com.survey_engine.user.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for company entity
 * HTTP requests and responses
 */
@RestController
@Validated
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Constructor for controller class
     * @param companyService - instance of service class
     */
    @Autowired
    public CompanyController(CompanyService companyService) {

        this.companyService = companyService;
    }

    /**
     * Method to create new company
     * @param companyRequest - request DTO
     * @return - HTTP response
     */
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest companyRequest) {
        CompanyResponse responseObject = companyService.createCompany(companyRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Method to retrieve company by id
     * @param id - company id
     * @return - HTTP response
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        CompanyResponse responseObject = companyService.findCompanyById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve company by name
     * @param name - company name
     * @return - HTTP response
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<CompanyResponse> getCompanyByName(@PathVariable String name) {
        CompanyResponse responseObject = companyService.findCompanyByName(name);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to retrieve all companies
     * @return - HTTP response
     */
    @GetMapping()
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<CompanyResponse> responseObject = companyService.findAllCompanies();
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to update company details
     * @param id - company id
     * @param companyRequest - request DTO
     * @return - HTTP response
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest companyRequest) {
        CompanyResponse responseObject = companyService.updateCompany(id, companyRequest);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Method to delete company
     * @param id - company id
     * @return - HTTP response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}