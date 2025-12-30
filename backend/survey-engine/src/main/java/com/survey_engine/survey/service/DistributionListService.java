package com.survey_engine.survey.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.survey_engine.survey.dto.ContactRequest;
import com.survey_engine.survey.dto.ContactResponse;
import com.survey_engine.survey.dto.DistributionListRequest;
import com.survey_engine.survey.dto.DistributionListResponse;
import com.survey_engine.survey.models.DistributionList;
import com.survey_engine.survey.models.DistributionListContact;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.DistributionListRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionListService {

    private final DistributionListRepository distributionListRepository;
    private final SurveyRepository surveyRepository;
    private final UserApi userApi;

    // Regex for Kenyan mobile numbers: Starts with +254 or 0, followed by 7 and 8 digits
    private static final Pattern KENYAN_PHONE_NUMBER_PATTERN = Pattern.compile("^(?:\\+254|0)?7[0-9]{8}$|^(?:\\+254|0)?1[0-9]{8}$");

    @Transactional
    public DistributionListResponse createDistributionList(String userId, DistributionListRequest request) {
        Long tenantId = userApi.getTenantId();
        
        DistributionList list = new DistributionList();
        list.setName(request.name());
        list.setTenantId(tenantId);
        list.setUserId(userId);
        
        if (request.contacts() != null) {
            List<DistributionListContact> contacts = request.contacts().stream()
                    .map(contactReq -> createContactFromRequest(contactReq, list))
                    .filter(c -> c != null) // Filter out invalid contacts
                    .collect(Collectors.toList());
            list.setContacts(contacts);
        }

        DistributionList savedList = distributionListRepository.save(list);
        return mapToResponse(savedList);
    }

    @Transactional
    public DistributionListResponse createDistributionListFromCsv(String userId, String name, MultipartFile file) {
        Long tenantId = userApi.getTenantId();

        DistributionList list = new DistributionList();
        list.setName(name);
        list.setTenantId(tenantId);
        list.setUserId(userId);

        List<DistributionListContact> contacts = parseCsv(file, list);
        list.setContacts(contacts);

        DistributionList savedList = distributionListRepository.save(list);
        return mapToResponse(savedList);
    }

    @Transactional(readOnly = true)
    public List<DistributionListResponse> getAllDistributionLists(String userId) {
        Long tenantId = userApi.getTenantId();
        String tenantName = userApi.findTenantNameById(tenantId).orElse("Main Tenant");

        List<DistributionList> lists;
        if ("Main Tenant".equalsIgnoreCase(tenantName) || "www".equalsIgnoreCase(tenantName)) {
            // Individual user: show only their own lists
            lists = distributionListRepository.findAllByTenantIdAndUserId(tenantId, userId);
        } else {
            // Enterprise user: show all lists in the tenant
            lists = distributionListRepository.findByTenantId(tenantId);
        }

        return lists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DistributionListResponse getDistributionListById(String userId, UUID id) {
        Long tenantId = userApi.getTenantId();
        DistributionList list = distributionListRepository.findByIdAndTenantIdAndUserId(id, tenantId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Distribution list not found"));
        return mapToResponse(list);
    }

    @Transactional
    public DistributionListResponse addContacts(String userId, UUID id, List<ContactRequest> contactRequests) {
        Long tenantId = userApi.getTenantId();
        DistributionList list = distributionListRepository.findByIdAndTenantIdAndUserId(id, tenantId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Distribution list not found"));

        if (contactRequests != null && !contactRequests.isEmpty()) {
            for (ContactRequest req : contactRequests) {
                String sanitizedPhone = sanitizePhoneNumber(req.phoneNumber());
                if (sanitizedPhone != null && !contactExists(list, sanitizedPhone)) {
                    DistributionListContact contact = new DistributionListContact();
                    contact.setDistributionList(list);
                    contact.setPhoneNumber(sanitizedPhone);
                    contact.setFirstName(req.firstName());
                    contact.setLastName(req.lastName());
                    contact.setEmail(req.email());
                    list.getContacts().add(contact);
                }
            }
        }
        
        DistributionList savedList = distributionListRepository.save(list);
        return mapToResponse(savedList);
    }

    /**
     * Adds a phone number to the distribution list associated with a specific survey.
     * This is primarily used by external triggers (e.g., payments).
     */
    @Transactional
    public void addContactToSurveyList(Long surveyId, String phoneNumber, String firstName, String lastName) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));

        DistributionList list = survey.getDistributionList();
        if (list == null) {
            list = new DistributionList();
            list.setName("Auto-Generated List for Survey: " + survey.getName() + " (" + survey.getId() + ")");
            list.setTenantId(survey.getTenantId());
            list.setUserId(survey.getUserId());
            list = distributionListRepository.save(list);
            
            survey.setDistributionList(list);
            surveyRepository.save(survey);
            log.info("Created new distribution list {} for survey {}", list.getId(), surveyId);
        }

        String sanitizedPhoneNumber = sanitizePhoneNumber(phoneNumber);
        if (sanitizedPhoneNumber != null && !contactExists(list, sanitizedPhoneNumber)) {
            DistributionListContact contact = new DistributionListContact();
            contact.setDistributionList(list);
            contact.setPhoneNumber(sanitizedPhoneNumber);
            contact.setFirstName(firstName);
            contact.setLastName(lastName);
            // Name/Email unknown for pure phone triggers unless provided, leaving null for now
            list.getContacts().add(contact);
            distributionListRepository.save(list);
            log.info("Added phone number {} to distribution list {} for survey {}", sanitizedPhoneNumber, list.getId(), surveyId);
        }
    }

    private DistributionListContact createContactFromRequest(ContactRequest req, DistributionList list) {
        String sanitized = sanitizePhoneNumber(req.phoneNumber());
        if (sanitized == null) return null;
        
        DistributionListContact contact = new DistributionListContact();
        contact.setDistributionList(list);
        contact.setPhoneNumber(sanitized);
        contact.setFirstName(req.firstName());
        contact.setLastName(req.lastName());
        contact.setEmail(req.email());
        return contact;
    }

    private boolean contactExists(DistributionList list, String phoneNumber) {
        return list.getContacts().stream()
                .anyMatch(c -> c.getPhoneNumber().equals(phoneNumber));
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        String cleanedNumber = phoneNumber.replaceAll("[^\\d+]", ""); // Remove non-digit except '+'

        if (KENYAN_PHONE_NUMBER_PATTERN.matcher(cleanedNumber).matches()) {
            if (cleanedNumber.startsWith("07")) {
                return "+254" + cleanedNumber.substring(1);
            }
            return cleanedNumber;
        }
        log.warn("Invalid phone number format: {}", phoneNumber);
        return null;
    }

    private List<DistributionListContact> parseCsv(MultipartFile file, DistributionList list) {
        List<DistributionListContact> contacts = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] header = csvReader.readNext();
            if (header == null) {
                return contacts;
            }

            // Identify column indices based on header names
            int phoneIndex = -1;
            int firstNameIndex = -1;
            int lastNameIndex = -1;
            int emailIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String col = header[i].toLowerCase().trim();
                if (col.contains("phone") || col.contains("mobile") || col.contains("number") || col.contains("tel")) {
                    phoneIndex = i;
                } else if (col.contains("first") || col.equals("name") || col.equals("fname")) {
                    firstNameIndex = i;
                } else if (col.contains("last") || col.contains("sur") || col.equals("lname")) {
                    lastNameIndex = i;
                } else if (col.contains("email") || col.contains("mail")) {
                    emailIndex = i;
                }
            }

            // Fallback to default indices if specific phone column not found
            if (phoneIndex == -1) {
                phoneIndex = 0;
                if (header.length > 1) firstNameIndex = 1;
                if (header.length > 2) lastNameIndex = 2;
                if (header.length > 3) emailIndex = 3;
            }

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length > phoneIndex) {
                    String phoneRaw = nextRecord[phoneIndex].trim();
                    String sanitized = sanitizePhoneNumber(phoneRaw);
                    
                    if (sanitized != null) {
                        DistributionListContact contact = new DistributionListContact();
                        contact.setDistributionList(list);
                        contact.setPhoneNumber(sanitized);
                        
                        if (firstNameIndex != -1 && nextRecord.length > firstNameIndex) {
                            contact.setFirstName(nextRecord[firstNameIndex].trim());
                        }
                        if (lastNameIndex != -1 && nextRecord.length > lastNameIndex) {
                            contact.setLastName(nextRecord[lastNameIndex].trim());
                        }
                        if (emailIndex != -1 && nextRecord.length > emailIndex) {
                            contact.setEmail(nextRecord[emailIndex].trim());
                        }
                        contacts.add(contact);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage());
        } catch (CsvValidationException e) {
            throw new RuntimeException("Failed to parse CSV file due to validation error: " + e.getMessage());
        }
        return contacts;
    }

    private DistributionListResponse mapToResponse(DistributionList list) {
        List<ContactResponse> contactResponses = list.getContacts().stream()
                .map(c -> new ContactResponse(c.getPhoneNumber(), c.getFirstName(), c.getLastName(), c.getEmail()))
                .collect(Collectors.toList());

        return new DistributionListResponse(
                list.getId(),
                list.getName(),
                contactResponses,
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }
}
