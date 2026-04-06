package com.hoang.backend.modules.adminpanel.service;

/**
 * Tạo sẵn tài khoản admin mặc định khi ứng dụng khởi động.
 */

import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_ADMIN_BOOTSTRAP_ENABLED:true}")
    private boolean bootstrapEnabled;

    @Value("${APP_ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${APP_ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${APP_ADMIN_FIRST_NAME}")
    private String adminFirstName;

    @Value("${APP_ADMIN_LAST_NAME}")
    private String adminLastName;

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        AppUser user = userRepository.findByUsernameIgnoreCase(adminUsername)
                .or(() -> userRepository.findByEmailIgnoreCase(adminEmail))
                .orElseGet(this::createAdminUser);

        boolean changed = false;

        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            user.setIsStaff(true);
            changed = true;
        }

        if (!Boolean.TRUE.equals(user.getIsSuperuser())) {
            user.setIsSuperuser(true);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
            log.info("Bootstrap admin privileges granted for user: {}", user.getUsername());
        }

        ensureAccountExists(user);
    }

    private AppUser createAdminUser() {
        AppUser user = new AppUser();
        user.setUsername(adminUsername.trim());
        user.setEmail(adminEmail.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setFirstName(adminFirstName.trim());
        user.setLastName(adminLastName.trim());
        user.setIsStaff(true);
        user.setIsSuperuser(true);

        AppUser saved = userRepository.save(user);
        log.info("Default admin account created: username='{}', email='{}'", saved.getUsername(), saved.getEmail());
        return saved;
    }

    private void ensureAccountExists(AppUser user) {
        accountRepository.findByUserId(user.getId()).orElseGet(() -> {
            Account account = new Account();
            account.setUser(user);
            account.setFirstName(nonNull(user.getFirstName()));
            account.setLastName(nonNull(user.getLastName()));
            account.setPhone("");
            return accountRepository.save(account);
        });
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }
}
