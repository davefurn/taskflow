package com.taskflow.api.repository.companySettings;

import com.taskflow.api.entity.CompanySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, UUID> {

    // POST /api/setup/init — is setup done?
    boolean existsBy();
}