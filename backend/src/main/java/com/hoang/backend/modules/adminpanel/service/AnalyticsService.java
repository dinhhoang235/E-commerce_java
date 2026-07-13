package com.hoang.backend.modules.adminpanel.service;

import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AppUserRepository userRepository;

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

    public List<Map<String, Object>> getTopProducts(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);
        return List.of();
    }

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

        return List.of(
                customerMetric("New Customers", newCurrent, calculateChange(newCurrent, newPrevious), newCurrent >= newPrevious ? "up" : "down"),
                customerMetric("Returning Customers", 0, "0%", "up"),
                customerMetric("Customer Retention", "0%", "0%", "up"),
                customerMetric("Avg. Order Value", "$0", "0%", "up")
        );
    }

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

    private void requireAdmin(String usernameOrEmail) {
        if (isBlank(usernameOrEmail)) {
            throw new IllegalArgumentException("User not found.");
        }
        AppUser user = userRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}
