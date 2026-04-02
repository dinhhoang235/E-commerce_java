package com.hoang.backend.modules.users.entity;

/**
 * Entity người dùng chính dùng cho xác thực và thông tin cơ bản.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String username;

    @Column(unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", length = 150)
    private String firstName = "";

    @Column(name = "last_name", length = 150)
    private String lastName = "";

    @Column(name = "date_joined", nullable = false)
    private LocalDateTime dateJoined;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @OneToOne(mappedBy = "user")
    private Account account;

    @OneToMany(mappedBy = "user")
    private List<Address> addresses = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (dateJoined == null) {
            dateJoined = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        if (dateJoined == null) {
            dateJoined = LocalDateTime.now();
        }
    }
}