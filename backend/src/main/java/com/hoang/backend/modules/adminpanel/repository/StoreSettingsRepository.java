package com.hoang.backend.modules.adminpanel.repository;

import com.hoang.backend.modules.adminpanel.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreSettingsRepository extends JpaRepository<StoreSettings, Long> {
}
