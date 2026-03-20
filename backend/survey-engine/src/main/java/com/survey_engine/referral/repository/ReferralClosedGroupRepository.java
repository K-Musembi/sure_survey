package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.ReferralClosedGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralClosedGroupRepository extends JpaRepository<ReferralClosedGroup, UUID> {

    List<ReferralClosedGroup> findByTenantId(Long tenantId);

    @Query(value = "SELECT COUNT(*) > 0 FROM referral_closed_group_members WHERE group_id = :groupId AND phone = :phone",
           nativeQuery = true)
    boolean isMember(@Param("groupId") UUID groupId, @Param("phone") String phone);
}
