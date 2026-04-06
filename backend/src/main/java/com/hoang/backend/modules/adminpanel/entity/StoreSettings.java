package com.hoang.backend.modules.adminpanel.entity;

/**
 * Entity cấu hình singleton cho cửa hàng (admin settings).
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store_settings")
public class StoreSettings {

    @Id
    private Long id = 1L;

    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName = "Apple Store";

    @Column(name = "store_description", nullable = false, columnDefinition = "TEXT")
    private String storeDescription = "Your trusted destination for the latest Apple products";

    @Column(name = "store_email", nullable = false, length = 254)
    private String storeEmail = "contact@applestore.com";

    @Column(name = "store_phone", nullable = false, length = 20)
    private String storePhone = "+1 (555) 123-4567";

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false, length = 50)
    private String timezone = "America/New_York";

    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    @Column(name = "order_notifications", nullable = false)
    private boolean orderNotifications = true;

    @Column(name = "inventory_alerts", nullable = false)
    private boolean inventoryAlerts = true;

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode;

    @Column(name = "allow_guest_checkout", nullable = false)
    private boolean allowGuestCheckout = true;

    @Column(name = "require_email_verification", nullable = false)
    private boolean requireEmailVerification;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = 1L;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
