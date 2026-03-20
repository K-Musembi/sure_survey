package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.ReferralInvite;
import com.survey_engine.referral.domain.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralInviteRepository extends JpaRepository<ReferralInvite, UUID> {

    @Query("""
        SELECT COUNT(i) > 0 FROM ReferralInvite i
        JOIN ReferralCode c ON c.id = i.referralCodeId
        WHERE i.referredPhone = :phone
          AND c.campaignId = :campaignId
          AND i.status NOT IN ('OPTED_OUT', 'INVALID', 'EXPIRED')
        """)
    boolean existsActiveInvite(@Param("phone") String phone, @Param("campaignId") UUID campaignId);

    @Query("""
        SELECT COUNT(i) FROM ReferralInvite i
        JOIN ReferralCode c ON c.id = i.referralCodeId
        WHERE c.referrerUserId = :referrerUserId
          AND c.campaignId = :campaignId
          AND i.inviteSentAt >= :since
        """)
    long countTodayInvites(@Param("referrerUserId") Long referrerUserId,
                           @Param("campaignId") UUID campaignId,
                           @Param("since") LocalDateTime since);

    List<ReferralInvite> findByReferralCodeId(UUID referralCodeId);

    List<ReferralInvite> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime now);

    List<ReferralInvite> findByReferredPhone(String referredPhone);
}
