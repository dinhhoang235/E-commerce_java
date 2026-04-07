package com.hoang.backend.modules.users.service;

/**
 * Service nghiệp vụ chính cho module users.
 * Xử lý đăng ký/đăng nhập, cập nhật account, đổi mật khẩu, địa chỉ và danh sách khách hàng admin.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.modules.users.dto.AccountResponse;
import com.hoang.backend.modules.users.dto.AddressRequest;
import com.hoang.backend.modules.users.dto.AddressResponse;
import com.hoang.backend.modules.users.dto.AdminCustomerResponse;
import com.hoang.backend.modules.users.dto.CustomerListResponse;
import com.hoang.backend.modules.users.dto.EmailAvailabilityResponse;
import com.hoang.backend.modules.users.dto.LoginRequest;
import com.hoang.backend.modules.users.dto.PasswordChangeRequest;
import com.hoang.backend.modules.users.dto.RegisterRequest;
import com.hoang.backend.modules.users.dto.RegisterResponse;
import com.hoang.backend.modules.users.dto.TokenResponse;
import com.hoang.backend.modules.users.dto.UsernameAvailabilityResponse;
import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.Address;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AddressRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public RegisterResponse register(RegisterRequest request) {
        // Kiểm tra dữ liệu đầu vào trước khi tạo user mới.
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
        // Hỗ trợ đăng nhập bằng username hoặc email.
        String identifier = safeTrim(request.username_or_email());
        String rawPassword = request.password() == null ? "" : request.password();

        AppUser user = findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this username/email."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
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
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
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
    public AccountResponse getCurrentAccount(String username) {
        AppUser user = requireUser(username);
        return toAccountResponse(user, latestAddress(user.getId()));
    }

    public AccountResponse updateCurrentAccount(String username, Map<String, Object> payload, MultipartFile avatarFile) throws IOException {
        // Cập nhật đồng thời thông tin account và địa chỉ mặc định (nếu có trong payload).
        AppUser user = requireUser(username);
        Account account = accountRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Account created = new Account();
                    created.setUser(user);
                    return accountRepository.save(created);
                });

        applyAccountUpdates(user, account, payload, avatarFile);
        Address address = updateAddressFromPayload(user, payload, true);

        return toAccountResponse(user, address != null ? address : latestAddress(user.getId()));
    }

    public void changePassword(String username, PasswordChangeRequest request) {
        AppUser user = requireUser(username);

        if (!passwordEncoder.matches(request.old_password(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        if (!Objects.equals(request.new_password(), request.confirm_password())) {
            throw new IllegalArgumentException("Password fields didn't match.");
        }

        user.setPassword(passwordEncoder.encode(request.new_password()));
        userRepository.save(user);
        tokenService.revokeUserTokens(user.getId());
    }

    public AddressResponse createOrUpdateAddress(String username, AddressRequest request) {
        AppUser user = requireUser(username);
        Address address = upsertAddress(user, request);
        return toAddressResponse(address, user.getEmail());
    }

    @Transactional(readOnly = true)
    public CustomerListResponse listCustomers(String search, String status, int page, int pageSize) {
        List<AppUser> users = new ArrayList<>(userRepository.findAllByOrderByDateJoinedDesc());

        if (search != null && !search.isBlank()) {
            String normalized = search.toLowerCase(Locale.ROOT);
            users = users.stream()
                    .filter(user -> containsIgnoreCase(user.getUsername(), normalized)
                            || containsIgnoreCase(user.getEmail(), normalized)
                            || containsIgnoreCase(user.getFirstName(), normalized)
                            || containsIgnoreCase(user.getLastName(), normalized))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            users = users.stream()
                    .filter(user -> status.equalsIgnoreCase(resolveStatus(user)))
                    .collect(Collectors.toList());
        }

        long total = users.size();
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = Math.min((safePage - 1) * safePageSize, users.size());
        int end = Math.min(start + safePageSize, users.size());

        List<AdminCustomerResponse> results = users.subList(start, end).stream()
                .map(this::toAdminCustomerResponse)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil(total / (double) safePageSize);
        return new CustomerListResponse(total, safePage, safePageSize, totalPages, results);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByAccessToken(String accessToken) {
        return tokenService.resolveAccessToken(accessToken)
                .flatMap(userRepository::findById);
    }

    @Transactional(readOnly = true)
    public AppUser requireUser(String username) {
        return findByUsernameOrEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
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
        if (!email.isBlank() && userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already in use.");
        }
    }

    private void applyAccountUpdates(AppUser user, Account account, Map<String, Object> payload, MultipartFile avatarFile) throws IOException {
        String firstName = stringValue(payload.get("first_name"), account.getFirstName());
        String lastName = stringValue(payload.get("last_name"), account.getLastName());
        String phone = stringValue(payload.get("phone"), account.getPhone());

        user.setFirstName(firstName);
        user.setLastName(lastName);
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setPhone(phone);

        if (payload.containsKey("avatar") && isBlankValue(payload.get("avatar"))) {
            account.setAvatar(null);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            account.setAvatar(storeAvatar(user.getId(), avatarFile));
        }

        userRepository.save(user);
        accountRepository.save(account);
    }

    private Address updateAddressFromPayload(AppUser user, Map<String, Object> payload, boolean allowMissing) {
        Object addressObject = payload.get("address");
        if (!(addressObject instanceof Map<?, ?> addressMap)) {
            return null;
        }

        Map<String, Object> addressData = new HashMap<>();
        addressMap.forEach((key, value) -> addressData.put(String.valueOf(key), value));
        AddressRequest request = objectMapper.convertValue(addressData, AddressRequest.class);
        return upsertAddress(user, request);
    }

    private Address upsertAddress(AppUser user, AddressRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Address payload is required.");
        }

        String addressLine1 = safeTrim(request.address_line1());
        String city = safeTrim(request.city());
        String state = safeTrim(request.state());
        String zipCode = safeTrim(request.zip_code());
        String country = normalizeCountry(request.country());

        Optional<Address> existing = addressRepository.findFirstByUserIdAndAddressLine1IgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndZipCodeIgnoreCaseAndCountryIgnoreCase(
                user.getId(), addressLine1, city, state, zipCode, country);

        Address address = existing.orElseGet(Address::new);
        address.setUser(user);
        address.setFirstName(safeTrim(request.first_name()));
        address.setLastName(safeTrim(request.last_name()));
        address.setPhone(safeTrim(request.phone()));
        address.setAddressLine1(addressLine1);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        address.setCountry(country);
        address.setDefault(Boolean.TRUE.equals(request.is_default()));

        return addressRepository.save(address);
    }

    private Address latestAddress(Long userId) {
        return addressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private AccountResponse toAccountResponse(AppUser user, Address address) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        AddressResponse addressResponse = address == null ? null : toAddressResponse(address, user.getEmail());
        String avatar = account == null ? null : account.getAvatarUrl();
        String phone = account == null ? "" : safeString(account.getPhone());

        return new AccountResponse(
                user.getId(),
                user.getUsername(),
                account == null ? user.getFirstName() : safeString(account.getFirstName()),
                account == null ? user.getLastName() : safeString(account.getLastName()),
                avatar,
                user.getEmail(),
                phone,
                addressResponse
        );
    }

    private AddressResponse toAddressResponse(Address address, String email) {
        return new AddressResponse(
                address.getId(),
                safeString(address.getFirstName()),
                safeString(address.getLastName()),
                safeString(address.getPhone()),
                safeString(address.getAddressLine1()),
                safeString(address.getCity()),
                safeString(address.getState()),
                safeString(address.getZipCode()),
                safeString(address.getCountry()),
                address.getCountryLabel(),
                address.getCreatedAt() == null ? null : ISO_FORMATTER.format(address.getCreatedAt()),
                address.isDefault(),
                email
        );
    }

    private AdminCustomerResponse toAdminCustomerResponse(AppUser user) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        Address locationAddress = latestAddress(user.getId());

        String name = buildName(account == null ? user.getFirstName() : account.getFirstName(), account == null ? user.getLastName() : account.getLastName(), user.getUsername());
        String location = locationAddress == null ? "No address" : locationAddress.getCity() + ", " + locationAddress.getState();
        String joinDate = user.getDateJoined() == null ? null : user.getDateJoined().toLocalDate().toString();

        return new AdminCustomerResponse(
                user.getId(),
                name,
                user.getEmail(),
                account == null ? "" : safeString(account.getPhone()),
                location,
                0,
                0.0,
                joinDate,
                resolveStatus(user)
        );
    }

    private String resolveStatus(AppUser user) {
        if (user.getLastLogin() == null) {
            return "inactive";
        }

        return user.getLastLogin().isAfter(LocalDateTime.now().minusDays(180)) ? "active" : "inactive";
    }

    private String buildName(String firstName, String lastName, String fallback) {
        String first = safeString(firstName);
        String last = safeString(lastName);

        if (!first.isBlank() && !last.isBlank()) {
            return first + " " + last;
        }
        if (!first.isBlank()) {
            return first;
        }
        if (!last.isBlank()) {
            return last;
        }
        return fallback;
    }

    private String storeAvatar(Long userId, MultipartFile file) throws IOException {
        // Lưu file vào thư mục uploads theo từng user để dễ quản lý.
        String safeFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "avatar").replaceAll("[^a-zA-Z0-9._-]", "_");
        Path uploadDir = Path.of("uploads", "user_" + userId);
        Files.createDirectories(uploadDir);
        Path destination = uploadDir.resolve(safeFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/user_" + userId + "/" + safeFileName;
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String normalizeEmail(String email) {
        return safeTrim(email).toLowerCase(Locale.ROOT);
    }

    private String normalizeCountry(String country) {
        String value = safeTrim(country).toUpperCase(Locale.ROOT);
        if (value.isBlank()) {
            return "VN";
        }

        return switch (value) {
            case "VIETNAM" -> "VN";
            case "UNITED STATES", "USA" -> "US";
            case "CANADA" -> "CA";
            case "UNITED KINGDOM" -> "GB";
            case "AUSTRALIA" -> "AU";
            case "SINGAPORE" -> "SG";
            case "JAPAN" -> "JP";
            default -> value;
        };
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) {
            return safeString(fallback);
        }

        String stringValue = value.toString().trim();
        return stringValue.isEmpty() ? safeString(fallback) : stringValue;
    }

    private boolean isBlankValue(Object value) {
        return value == null || value.toString().isBlank();
    }

}