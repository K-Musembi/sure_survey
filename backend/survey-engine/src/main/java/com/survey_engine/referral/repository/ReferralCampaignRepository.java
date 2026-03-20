package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.ReferralCampaign;
import com.survey_engine.referral.domain.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralCampaignRepository extends JpaRepository<ReferralCampaign, UUID> {

    List<ReferralCampaign> findByTenantId(Long tenantId);

    List<ReferralCampaign> findBySurveyIdAndStatus(Long surveyId, CampaignStatus status);

    List<ReferralCampaign> findByTenantIdAndStatus(Long tenantId, CampaignStatus status);
}
