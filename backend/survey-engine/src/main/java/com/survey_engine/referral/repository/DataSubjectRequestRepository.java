package com.survey_engine.referral.repository;

import com.survey_engine.referral.domain.DataSubjectRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataSubjectRequestRepository extends JpaRepository<DataSubjectRequest, UUID> {

    List<DataSubjectRequest> findAllByOrderByRequestedAtDesc();

    @Query("SELECT COUNT(d) FROM DataSubjectRequest d WHERE d.phoneHash = :phoneHash AND d.requestedAt >= :since")
    long countRecentByPhoneHash(@Param("phoneHash") String phoneHash,
                                @Param("since") LocalDateTime since);
}
