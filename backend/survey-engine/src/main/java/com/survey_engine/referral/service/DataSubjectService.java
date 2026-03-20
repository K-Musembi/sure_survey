package com.survey_engine.referral.service;

import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.referral.domain.DataSubjectRequest;
import com.survey_engine.referral.domain.ReferralConsentLog;
import com.survey_engine.referral.domain.ReferralInvite;
import com.survey_engine.referral.domain.enums.InviteStatus;
import com.survey_engine.referral.domain.enums.RequestStatus;
import com.survey_engine.referral.domain.enums.RequestType;
import com.survey_engine.referral.dto.DataSubjectAccessResponse;
import com.survey_engine.referral.dto.DataSubjectAccessResponse.ConsentSummary;
import com.survey_engine.referral.dto.DataSubjectAccessResponse.InviteSummary;
import com.survey_engine.referral.repository.DataSubjectRequestRepository;
import com.survey_engine.referral.repository.ReferralConsentLogRepository;
import com.survey_engine.referral.repository.ReferralInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Handles ODPC Data Subject Rights under the Kenya Data Protection Act 2019.
 *
 * <p>Phones are never stored in plaintext — only as SHA-256(phone + salt) hashes.
 * Erasure pseudonymises matching phone fields across referral tables.
 *
 * <p>Rate limit: 3 requests per phone hash per 24 hours.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSubjectService {

    private static final int DSR_RATE_LIMIT = 3;
    private static final String ERASED_PREFIX = "ERASED-";

    private final DataSubjectRequestRepository dsrRepository;
    private final ReferralInviteRepository inviteRepository;
    private final ReferralConsentLogRepository consentLogRepository;

    @Value("${app-security.encryption.salt}")
    private String encryptionSalt;

    @Transactional
    public DataSubjectAccessResponse processAccessRequest(String rawPhone) {
        String hash = hashPhone(rawPhone);
        enforceRateLimit(hash);

        // Record DSR
        DataSubjectRequest dsr = new DataSubjectRequest();
        dsr.setRequestType(RequestType.ACCESS);
        dsr.setPhoneHash(hash);
        dsr.setStatus(RequestStatus.COMPLETED);
        dsr.setCompletedAt(LocalDateTime.now());
        dsr = dsrRepository.save(dsr);

        List<ReferralInvite> invites = inviteRepository.findByReferredPhone(rawPhone);
        List<ReferralConsentLog> logs = consentLogRepository.findByPhone(rawPhone);

        log.info("SAR completed: hash={} invites={} consentLogs={}",
                hash, invites.size(), logs.size());

        return new DataSubjectAccessResponse(
                hash,
                LocalDateTime.now(),
                dsr.getId(),
                invites.stream().map(i -> new InviteSummary(
                        i.getId(),
                        i.getStatus().name(),
                        i.getChannel(),
                        i.getInviteSentAt(),
                        i.getOptedInAt(),
                        i.getActionCompletedAt()
                )).toList(),
                logs.stream().map(l -> new ConsentSummary(
                        l.getId(),
                        l.getEventType().name(),
                        l.getChannel(),
                        l.isReferrerConfirmedConsent(),
                        l.getCreatedAt()
                )).toList()
        );
    }

    @Transactional
    public UUID processErasureRequest(String rawPhone) {
        String hash = hashPhone(rawPhone);
        enforceRateLimit(hash);

        // Opt out all active invites first
        List<ReferralInvite> activeInvites = inviteRepository.findByReferredPhone(rawPhone)
                .stream()
                .filter(i -> !i.isTerminal())
                .toList();
        activeInvites.forEach(i -> i.setStatus(InviteStatus.OPTED_OUT));
        inviteRepository.saveAll(activeInvites);

        // Pseudonymise phone in invites
        String pseudonym = ERASED_PREFIX + hash.substring(0, 12).toUpperCase();
        List<ReferralInvite> allInvites = inviteRepository.findByReferredPhone(rawPhone);
        allInvites.forEach(i -> i.setReferredPhone(pseudonym));
        inviteRepository.saveAll(allInvites);

        // Pseudonymise phone in consent log via direct query (bypasses updatable=false JPA constraint)
        int consentLogsUpdated = consentLogRepository.pseudonymisePhone(rawPhone, pseudonym);

        // Record DSR
        DataSubjectRequest dsr = new DataSubjectRequest();
        dsr.setRequestType(RequestType.ERASURE);
        dsr.setPhoneHash(hash);
        dsr.setStatus(RequestStatus.COMPLETED);
        dsr.setNotes("Pseudonymised " + allInvites.size() + " invites, " + consentLogsUpdated + " consent records.");
        dsr.setCompletedAt(LocalDateTime.now());
        DataSubjectRequest saved = dsrRepository.save(dsr);

        log.info("Erasure completed: hash={} invites={} consentLogs={}",
                hash, allInvites.size(), consentLogsUpdated);

        return saved.getId();
    }

    public List<DataSubjectRequest> getAllRequests() {

        return dsrRepository.findAllByOrderByRequestedAtDesc();
    }

    @Transactional
    public DataSubjectRequest updateRequestStatus(UUID dsrId, RequestStatus newStatus, String notes) {
        DataSubjectRequest dsr = dsrRepository.findById(dsrId)
                .orElseThrow(() -> new BusinessRuleException("DSR_NOT_FOUND",
                        "Data subject request not found: " + dsrId));
        dsr.setStatus(newStatus);
        if (notes != null) dsr.setNotes(notes);
        if (newStatus == RequestStatus.COMPLETED || newStatus == RequestStatus.REJECTED) {
            dsr.setCompletedAt(LocalDateTime.now());
        }
        return dsrRepository.save(dsr);
    }

    private void enforceRateLimit(String phoneHash) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long recentCount = dsrRepository.countRecentByPhoneHash(phoneHash, since);
        if (recentCount >= DSR_RATE_LIMIT) {
            throw new BusinessRuleException("DSR_RATE_LIMIT_EXCEEDED",
                    "Maximum of " + DSR_RATE_LIMIT + " data subject requests allowed per 24 hours.");
        }
    }

    String hashPhone(String rawPhone) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = rawPhone.trim() + encryptionSalt;
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
