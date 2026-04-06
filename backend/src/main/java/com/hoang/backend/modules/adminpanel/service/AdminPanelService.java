package com.hoang.backend.modules.adminpanel.service;

/**
 * Service cho các nghiệp vụ admin panel: login admin, analytics, settings, payments stats.
 */

import com.hoang.backend.modules.adminpanel.dto.AdminLoginRequest;
import com.hoang.backend.modules.adminpanel.dto.AdminLoginResponse;
import com.hoang.backend.modules.adminpanel.entity.StoreSettings;
import com.hoang.backend.modules.adminpanel.repository.StoreSettingsRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPanelService {

    private static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<String> CURRENCIES = Set.of("USD", "EUR", "GBP", "CAD");
    private static final Set<String> TIMEZONES = Set.of(
            "America/New_York",
            "America/Chicago",
            "America/Denver",
            "America/Los_Angeles"
    );

    private final AppUserRepository userRepository;
    private final StoreSettingsRepository storeSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(AdminLoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Email/username and password are required");
        }

        AppUser user = findByUsernameOrEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials or insufficient permissions"));

        if (!passwordEncoder.matches(request.password(), user.getPassword()) || !Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }

        String fullName = (safe(user.getFirstName()) + " " + safe(user.getLastName())).trim();
        String displayName = fullName.isBlank() ? safe(user.getUsername()) : fullName;
        String role = Boolean.TRUE.equals(user.getIsSuperuser()) ? "admin" : "manager";
        return new AdminLoginResponse(user.getId(), safe(user.getEmail()), displayName, role);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSalesAnalytics(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        List<Map<String, Object>> sales = new ArrayList<>();

        sales.add(salesItem("Today", 0.0, 0, "0%"));
        sales.add(salesItem("Yesterday", 0.0, 0, "0%"));
        sales.add(salesItem("This Week", 0.0, 0, "0%"));
        sales.add(salesItem("Last Week", 0.0, 0, "0%"));
        sales.add(salesItem("This Month", 0.0, 0, "0%"));
        sales.add(salesItem("Last Month", 0.0, 0, "0%"));

        return sales;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopProducts(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return List.of();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductStats(String authenticatedUsername, Long productId) {
        requireAdmin(authenticatedUsername);

        int baseViews = 100 + Math.max(productId == null ? 0 : productId.intValue(), 0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSales", 0);
        result.put("revenue", 0.0);
        result.put("pageViews", baseViews);
        result.put("conversionRate", "0.0%");
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerMetrics(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        LocalDate today = LocalDate.now();
        LocalDateTime lastMonth = today.minusDays(30).atStartOfDay();
        LocalDateTime twoMonthsAgo = today.minusDays(60).atStartOfDay();

        List<AppUser> allUsers = userRepository.findAll();
        long newCurrent = allUsers.stream()
                .filter(user -> user.getDateJoined() != null && !user.getDateJoined().isBefore(lastMonth))
                .count();
        long newPrevious = allUsers.stream()
                .filter(user -> user.getDateJoined() != null
                        && !user.getDateJoined().isBefore(twoMonthsAgo)
                        && user.getDateJoined().isBefore(lastMonth))
                .count();

        double retentionRate = 0.0;

        return List.of(
                customerMetric("New Customers", newCurrent, calculateChange(newCurrent, newPrevious), newCurrent >= newPrevious ? "up" : "down"),
                customerMetric("Returning Customers", 0, "0%", "up"),
                customerMetric("Customer Retention", String.format(Locale.ROOT, "%.0f%%", retentionRate), "0%", "up"),
                customerMetric("Avg. Order Value", "$0", "0%", "up")
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrafficSources(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return List.of(
                trafficSource("Direct", 4520, 45),
                trafficSource("Google Search", 3210, 32),
                trafficSource("Social Media", 1340, 13),
                trafficSource("Email", 890, 9),
                trafficSource("Referrals", 240, 2)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getConversionRate(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rate", "0.0%");
        result.put("change", "0%");
        result.put("trend", "up");
        result.put("today_orders", 0);
        result.put("today_sessions", 100);
        result.put("yesterday_orders", 0);
        result.put("yesterday_sessions", 95);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("salesData", getSalesAnalytics(authenticatedUsername));
        dashboard.put("topProducts", getTopProducts(authenticatedUsername));
        dashboard.put("customerMetrics", getCustomerMetrics(authenticatedUsername));
        dashboard.put("trafficSources", getTrafficSources(authenticatedUsername));
        dashboard.put("conversionRate", getConversionRate(authenticatedUsername));
        return dashboard;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPaymentTransactions(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return List.of();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStats(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_transactions", 0);
        result.put("total_amount", 0.0);
        result.put("successful_transactions", 0);
        result.put("pending_transactions", 0);
        result.put("failed_transactions", 0);
        result.put("refunded_transactions", 0);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStoreSettings(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return toStoreSettingsPayload(resolveSettings());
    }

    public Map<String, Object> updateStoreSettings(String authenticatedUsername, Map<String, Object> payload, String section) {
        requireAdmin(authenticatedUsername);

        StoreSettings settings = resolveSettings();
        Map<String, Object> updates = payload == null ? Map.of() : new LinkedHashMap<>(payload);
        applySettingsUpdate(settings, updates, section);

        StoreSettings saved = storeSettingsRepository.save(settings);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Settings" + (isBlank(section) ? "" : " section " + section) + " updated successfully");
        response.put("data", toStoreSettingsPayload(saved));
        return response;
    }

    @Transactional(readOnly = true)
    public AppUser requireAdmin(String usernameOrEmail) {
        AppUser user = findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }
        return user;
    }

    private java.util.Optional<AppUser> findByUsernameOrEmail(String value) {
        if (isBlank(value)) {
            return java.util.Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(value)
                .or(() -> userRepository.findByEmailIgnoreCase(value));
    }

    private StoreSettings resolveSettings() {
        return storeSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    StoreSettings created = new StoreSettings();
                    created.setId(1L);
                    return storeSettingsRepository.save(created);
                });
    }

    private void applySettingsUpdate(StoreSettings settings, Map<String, Object> payload, String section) {
        Set<String> allowedFields = switch (section == null ? "" : section.toLowerCase(Locale.ROOT)) {
            case "general" -> Set.of("store_name", "store_description", "store_email", "store_phone", "currency", "timezone");
            case "notifications" -> Set.of("email_notifications", "order_notifications", "inventory_alerts");
            case "security" -> Set.of("maintenance_mode", "allow_guest_checkout", "require_email_verification");
            default -> Set.of(
                    "store_name", "store_description", "store_email", "store_phone", "currency", "timezone",
                    "email_notifications", "order_notifications", "inventory_alerts",
                    "maintenance_mode", "allow_guest_checkout", "require_email_verification"
            );
        };

        if (payload.containsKey("store_name") && allowedFields.contains("store_name")) {
            String value = safe(payload.get("store_name")).trim();
            if (value.length() < 2) {
                throw new IllegalArgumentException("Store name must be at least 2 characters long.");
            }
            settings.setStoreName(value);
        }

        if (payload.containsKey("store_description") && allowedFields.contains("store_description")) {
            settings.setStoreDescription(safe(payload.get("store_description")));
        }

        if (payload.containsKey("store_email") && allowedFields.contains("store_email")) {
            String email = safe(payload.get("store_email")).trim();
            if (email.isBlank() || !email.contains("@") || !email.substring(email.indexOf('@')).contains(".")) {
                throw new IllegalArgumentException("Store email is required.");
            }
            settings.setStoreEmail(email);
        }

        if (payload.containsKey("store_phone") && allowedFields.contains("store_phone")) {
            String phone = safe(payload.get("store_phone")).trim();
            if (!phone.isBlank() && phone.length() < 10) {
                throw new IllegalArgumentException("Please enter a valid phone number.");
            }
            settings.setStorePhone(phone);
        }

        if (payload.containsKey("currency") && allowedFields.contains("currency")) {
            String currency = safe(payload.get("currency")).trim().toUpperCase(Locale.ROOT);
            if (!CURRENCIES.contains(currency)) {
                throw new IllegalArgumentException("Invalid currency value.");
            }
            settings.setCurrency(currency);
        }

        if (payload.containsKey("timezone") && allowedFields.contains("timezone")) {
            String timezone = safe(payload.get("timezone")).trim();
            if (!TIMEZONES.contains(timezone)) {
                throw new IllegalArgumentException("Invalid timezone value.");
            }
            settings.setTimezone(timezone);
        }

        if (payload.containsKey("email_notifications") && allowedFields.contains("email_notifications")) {
            settings.setEmailNotifications(toBoolean(payload.get("email_notifications")));
        }

        if (payload.containsKey("order_notifications") && allowedFields.contains("order_notifications")) {
            settings.setOrderNotifications(toBoolean(payload.get("order_notifications")));
        }

        if (payload.containsKey("inventory_alerts") && allowedFields.contains("inventory_alerts")) {
            settings.setInventoryAlerts(toBoolean(payload.get("inventory_alerts")));
        }

        if (payload.containsKey("maintenance_mode") && allowedFields.contains("maintenance_mode")) {
            settings.setMaintenanceMode(toBoolean(payload.get("maintenance_mode")));
        }

        if (payload.containsKey("allow_guest_checkout") && allowedFields.contains("allow_guest_checkout")) {
            settings.setAllowGuestCheckout(toBoolean(payload.get("allow_guest_checkout")));
        }

        if (payload.containsKey("require_email_verification") && allowedFields.contains("require_email_verification")) {
            settings.setRequireEmailVerification(toBoolean(payload.get("require_email_verification")));
        }
    }

    private Map<String, Object> toStoreSettingsPayload(StoreSettings settings) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("store_name", settings.getStoreName());
        payload.put("store_description", settings.getStoreDescription());
        payload.put("store_email", settings.getStoreEmail());
        payload.put("store_phone", settings.getStorePhone());
        payload.put("currency", settings.getCurrency());
        payload.put("timezone", settings.getTimezone());
        payload.put("email_notifications", settings.isEmailNotifications());
        payload.put("order_notifications", settings.isOrderNotifications());
        payload.put("inventory_alerts", settings.isInventoryAlerts());
        payload.put("maintenance_mode", settings.isMaintenanceMode());
        payload.put("allow_guest_checkout", settings.isAllowGuestCheckout());
        payload.put("require_email_verification", settings.isRequireEmailVerification());
        payload.put("created_at", formatTime(settings.getCreatedAt()));
        payload.put("updated_at", formatTime(settings.getUpdatedAt()));
        return payload;
    }

    private Map<String, Object> salesItem(String period, double revenue, int orders, String change) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("period", period);
        item.put("revenue", revenue);
        item.put("orders", orders);
        item.put("change", change);
        return item;
    }

    private Map<String, Object> customerMetric(String metric, Object value, String change, String trend) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("metric", metric);
        item.put("value", value);
        item.put("change", change);
        item.put("trend", trend);
        return item;
    }

    private Map<String, Object> trafficSource(String source, int visitors, int percentage) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", source);
        item.put("visitors", visitors);
        item.put("percentage", percentage);
        return item;
    }

    private String calculateChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }

        BigDecimal currentVal = BigDecimal.valueOf(current);
        BigDecimal previousVal = BigDecimal.valueOf(previous);
        BigDecimal change = currentVal.subtract(previousVal)
                .multiply(BigDecimal.valueOf(100))
                .divide(previousVal, 0, java.math.RoundingMode.HALF_UP);

        String prefix = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return prefix + change + "%";
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return ISO_TIME_FORMATTER.format(time);
    }
}
