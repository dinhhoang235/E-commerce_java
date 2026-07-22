package com.hoang.backend.modules.adminpanel.service;

import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.modules.adminpanel.entity.StoreSettings;
import com.hoang.backend.modules.adminpanel.repository.StoreSettingsRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreSettingsService {

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
        response.put("message", "Settings" + (blank(section) ? "" : " section " + section) + " updated successfully");
        response.put("data", toStoreSettingsPayload(saved));
        return response;
    }

    private StoreSettings resolveSettings() {
        return storeSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    StoreSettings created = new StoreSettings();
                    created.setId(1L);
                    return storeSettingsRepository.save(created);
                });
    }

    private void requireAdmin(String usernameOrEmail) {
        if (blank(usernameOrEmail)) {
            throw new UserNotFoundException("null");
        }
        AppUser user = userRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new UserNotFoundException(usernameOrEmail));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new UnauthorizedException();
        }
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
            String value = safeObject(payload.get("store_name")).trim();
            if (value.length() < 2) {
                throw new IllegalArgumentException("Store name must be at least 2 characters long.");
            }
            settings.setStoreName(value);
        }

        if (payload.containsKey("store_description") && allowedFields.contains("store_description")) {
            settings.setStoreDescription(safeObject(payload.get("store_description")));
        }

        if (payload.containsKey("store_email") && allowedFields.contains("store_email")) {
            String email = safeObject(payload.get("store_email")).trim();
            if (email.isBlank() || !email.contains("@") || !email.substring(email.indexOf('@')).contains(".")) {
                throw new IllegalArgumentException("Store email is required.");
            }
            settings.setStoreEmail(email);
        }

        if (payload.containsKey("store_phone") && allowedFields.contains("store_phone")) {
            String phone = safeObject(payload.get("store_phone")).trim();
            if (!phone.isBlank() && phone.length() < 10) {
                throw new IllegalArgumentException("Please enter a valid phone number.");
            }
            settings.setStorePhone(phone);
        }

        if (payload.containsKey("currency") && allowedFields.contains("currency")) {
            String currency = safeObject(payload.get("currency")).trim().toUpperCase(Locale.ROOT);
            if (!CURRENCIES.contains(currency)) {
                throw new IllegalArgumentException("Invalid currency value.");
            }
            settings.setCurrency(currency);
        }

        if (payload.containsKey("timezone") && allowedFields.contains("timezone")) {
            String timezone = safeObject(payload.get("timezone")).trim();
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

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return ISO_TIME_FORMATTER.format(time);
    }
}
