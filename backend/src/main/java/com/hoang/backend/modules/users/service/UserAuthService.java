package com.hoang.backend.modules.users.service;

import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.modules.users.dto.EmailAvailabilityResponse;
import com.hoang.backend.modules.users.dto.LoginRequest;
import com.hoang.backend.modules.users.dto.RegisterRequest;
import com.hoang.backend.modules.users.dto.RegisterResponse;
import com.hoang.backend.modules.users.dto.TokenResponse;
import com.hoang.backend.modules.users.dto.UsernameAvailabilityResponse;
import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthService {

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public RegisterResponse register(RegisterRequest request) {
        validateRegistration(request);

        AppUser user = new AppUser();
        user.setUsername(request.username().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(safeTrim(request.first_name()));
        user.setLastName(safeTrim(request.last_name()));
        user.setIsStaff(false);
        user.setIsSuperuser(false);
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setFirstName(user.getFirstName());
        account.setLastName(user.getLastName());
        account.setPhone(safeTrim(request.phone()));
        account.setAvatar(null);
        accountRepository.save(account);

        TokenResponse token = tokenService.issueTokens(user);
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getPhone(),
                token
        );
    }

    public TokenResponse login(LoginRequest request) {
        String identifier = safeTrim(request.username_or_email());
        String rawPassword = request.password() == null ? "" : request.password();

        AppUser user = findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UserNotFoundException(identifier));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UnauthorizedException();
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return tokenService.issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        Long userId = tokenService.resolveRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token."));

        tokenService.revokeUserTokens(userId);
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
        return tokenService.issueTokens(user);
    }

    public UsernameAvailabilityResponse checkUsernameAvailability(String username) {
        String normalized = safeTrim(username);
        if (normalized.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (!normalized.replace("_", "").chars().allMatch(Character::isLetterOrDigit)) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }

        return new UsernameAvailabilityResponse(normalized, !userRepository.existsByUsernameIgnoreCase(normalized));
    }

    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        String normalized = normalizeEmail(email);
        if (normalized.isBlank() || !normalized.contains("@") || !normalized.substring(normalized.indexOf('@')).contains(".")) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }

        return new EmailAvailabilityResponse(normalized, !userRepository.existsByEmailIgnoreCase(normalized));
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByAccessToken(String accessToken) {
        return tokenService.resolveAccessToken(accessToken)
                .flatMap(userRepository::findById);
    }

    public AppUser requireUser(String username) {
        return findByUsernameOrEmail(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private Optional<AppUser> findByUsernameOrEmail(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByUsernameIgnoreCase(value)
                .or(() -> userRepository.findByEmailIgnoreCase(value));
    }

    private void validateRegistration(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration payload is required.");
        }

        if (request.username() == null || request.username().trim().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        if (!Objects.equals(request.password(), request.confirm_password())) {
            throw new IllegalArgumentException("Password fields didn't match.");
        }

        if (userRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new IllegalArgumentException("This username is already taken.");
        }

        String email = normalizeEmail(request.email());
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already in use.");
        }
    }

    private String normalizeEmail(String email) {
        return safeTrim(email).toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
