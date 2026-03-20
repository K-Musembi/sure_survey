package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.ReferralConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralConsentLogRepository extends JpaRepository<ReferralConsentLog, UUID> {

    List<ReferralConsentLog> findByPhone(String phone);

    List<ReferralConsentLog> findByReferralInviteId(UUID referralInviteId);

    /**
     * Pseudonymises the phone field across all consent log records matching rawPhone.
     * Uses a direct JPQL update to bypass the JPA updatable=false constraint on
     * the phone column — this is the only permitted mutation of this field under
     * an ODPC erasure request.
     */
    @Modifying
    @Query("UPDATE ReferralConsentLog c SET c.phone = :pseudonym WHERE c.phone = :rawPhone")
    int pseudonymisePhone(@Param("rawPhone") String rawPhone, @Param("pseudonym") String pseudonym);
}
