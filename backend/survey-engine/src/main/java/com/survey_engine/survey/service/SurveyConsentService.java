package com.survey_engine.survey.service;

import com.survey_engine.common.enums.SettingKey;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.common.exception.ResourceNotFoundException;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.models.SurveyConsentLog;
import com.survey_engine.survey.repository.SurveyConsentLogRepository;
import com.survey_engine.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Manages ODPC-compliant consent for all survey channels.
 * For SMS/USSD/WhatsApp: consent must be recorded before the first question.
 * For Web/Mobile: consent acknowledgment is submitted with the response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyConsentService {

    private final SurveyConsentLogRepository consentLogRepository;
    private final SurveyRepository surveyRepository;
    private final SystemSettingRepository settingRepository;

    @Value("${app-security.encryption.salt:default-salt}")
    private String encryptionSalt;

    /**
     * Records consent (opt-in or opt-out) for a survey respondent.
     */
    @Transactional
    public void recordConsent(Long surveyId, String phone, String participantId,
                              String eventType, String channel) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("SURVEY_NOT_FOUND",
                        "Survey not found: " + surveyId));

        String privacyUrl = settingRepository.findByKey(SettingKey.PRIVACY_NOTICE_URL)
                .map(s -> s.getValue())
                .orElse(null);

        SurveyConsentLog entry = new SurveyConsentLog();
        entry.setSurveyId(surveyId);
        entry.setEventType(eventType);
        entry.setChannel(channel);
        entry.setConsentMessage(survey.getConsentMessage());
        entry.setPrivacyNoticeUrl(privacyUrl);

        if (phone != null) {
            entry.setPhoneHash(hashPhone(phone));
        }
        if (participantId != null) {
            entry.setParticipantId(participantId);
        }

        consentLogRepository.save(entry);
        log.info("Recorded {} consent for survey {} channel {}", eventType, surveyId, channel);
    }

    /**
     * Validates that consent has been recorded for a non-web channel response.
     * Web responses include consent acknowledgment in the payload.
     */
    public void validateConsent(Long surveyId, String phone, String participantId, String channel) {
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null || !survey.isRequiresConsent()) return;

        if ("WEB".equalsIgnoreCase(channel) || "MOBILE".equalsIgnoreCase(channel)) {
            // Web/mobile consent is validated via the consentAcknowledged flag in request
            return;
        }

        // For SMS/USSD/WhatsApp, check consent log
        boolean hasConsent = false;
        if (phone != null) {
            String phoneHash = hashPhone(phone);
            hasConsent = consentLogRepository.existsBySurveyIdAndPhoneHashAndEventType(
                    surveyId, phoneHash, "OPT_IN");
        } else if (participantId != null) {
            hasConsent = consentLogRepository.existsBySurveyIdAndParticipantIdAndEventType(
                    surveyId, participantId, "OPT_IN");
        }

        if (!hasConsent) {
            throw new BusinessRuleException("CONSENT_REQUIRED",
                    "Survey response requires prior consent. Please opt-in before responding.");
        }
    }

    /**
     * Builds the default consent message for a survey.
     */
    public String getDefaultConsentMessage(String surveyName) {
        return "Thank you! We would like you to take a short survey: '"
                + surveyName + "'. To proceed enter 1 (YES). To opt-out enter 2 (NO).";
    }

    private String hashPhone(String phone) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((phone.trim() + encryptionSalt).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            log.error("Failed to hash phone number", e);
            return phone; // Fallback — should never happen with SHA-256
        }
    }
}
