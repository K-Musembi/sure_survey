package com.survey_engine.common.repository;

import com.survey_engine.common.models.SystemSetting;
import com.survey_engine.common.enums.SettingKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing system settings.
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, SettingKey> {
    Optional<SystemSetting> findByKey(SettingKey key);
}