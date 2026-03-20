package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {

    Optional<ReferralCode> findByCampaignIdAndReferrerUserId(UUID campaignId, Long referrerUserId);

    Optional<ReferralCode> findByCode(String code);
}
