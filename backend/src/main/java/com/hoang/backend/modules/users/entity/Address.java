package com.hoang.backend.modules.users.entity;

/**
 * Entity địa chỉ giao hàng của người dùng.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "first_name", length = 150)
    private String firstName = "";

    @Column(name = "last_name", length = 150)
    private String lastName = "";

    @Column(length = 15)
    private String phone = "";

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;

    @Column(nullable = false, length = 100)
    private String country = "VN";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getCountryLabel() {
        // Chuyển mã quốc gia sang nhãn dễ đọc cho phía frontend.
        return switch (country == null ? "VN" : country) {
            case "US" -> "United States";
            case "CA" -> "Canada";
            case "GB" -> "United Kingdom";
            case "AU" -> "Australia";
            case "SG" -> "Singapore";
            case "JP" -> "Japan";
            default -> "Vietnam";
        };
    }
}