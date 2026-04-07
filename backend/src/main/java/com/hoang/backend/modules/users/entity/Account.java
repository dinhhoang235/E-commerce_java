package com.hoang.backend.modules.users.entity;

/**
 * Entity thông tin mở rộng của người dùng (họ tên, avatar, số điện thoại).
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "first_name", length = 150)
    private String firstName = "";

    @Column(name = "last_name", length = 150)
    private String lastName = "";

    @Column(length = 500)
    private String avatar;

    @Column(length = 15)
    private String phone = "";

    public String getAvatarUrl() {
        // Trả về null khi người dùng chưa upload avatar.
        if (avatar == null || avatar.isBlank()) {
            return null;
        }
        return avatar;
    }
}