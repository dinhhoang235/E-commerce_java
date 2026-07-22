package com.hoang.backend.modules.adminpanel.service;

import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.modules.adminpanel.dto.AdminLoginRequest;
import com.hoang.backend.modules.adminpanel.dto.AdminLoginResponse;
import com.hoang.backend.modules.payments.service.PaymentQueryService;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPanelService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentQueryService paymentQueryService;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(AdminLoginRequest request) {
        if (request == null || blank(request.email()) || blank(request.password())) {
            throw new IllegalArgumentException("Email/username and password are required");
        }

        AppUser user = findByUsernameOrEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials or insufficient permissions"));

        if (!passwordEncoder.matches(request.password(), user.getPassword()) || !Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }

        String fullName = (safeObject(user.getFirstName()) + " " + safeObject(user.getLastName())).trim();
        String displayName = fullName.isBlank() ? safeObject(user.getUsername()) : fullName;
        String role = Boolean.TRUE.equals(user.getIsSuperuser()) ? "admin" : "manager";
        return new AdminLoginResponse(user.getId(), safeObject(user.getEmail()), displayName, role);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPaymentTransactions(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return paymentQueryService.listAdminTransactions();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStats(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return paymentQueryService.adminPaymentStats();
    }

    private void requireAdmin(String usernameOrEmail) {
        AppUser user = findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException(usernameOrEmail));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new UnauthorizedException();
        }
    }

    private java.util.Optional<AppUser> findByUsernameOrEmail(String value) {
        if (blank(value)) {
            return java.util.Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(value)
                .or(() -> userRepository.findByEmailIgnoreCase(value));
    }
}
