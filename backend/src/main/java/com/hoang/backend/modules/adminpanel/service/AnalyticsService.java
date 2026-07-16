package com.hoang.backend.modules.adminpanel.service;

import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<Map<String, Object>> getSalesAnalytics(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisMonthStart = today.withDayOfMonth(1);

        List<Map<String, Object>> sales = new ArrayList<>();

        sales.add(buildSalesItem("Today",
                today.atStartOfDay(), today.minusDays(1).atStartOfDay()));

        sales.add(buildSalesItem("Yesterday",
                today.minusDays(1).atStartOfDay(), today.minusDays(2).atStartOfDay()));

        sales.add(buildSalesItem("This Week",
                thisWeekStart.atStartOfDay(), thisWeekStart.minusWeeks(1).atStartOfDay()));

        sales.add(buildSalesItem("Last Week",
                thisWeekStart.minusWeeks(1).atStartOfDay(), thisWeekStart.minusWeeks(2).atStartOfDay()));

        sales.add(buildSalesItem("This Month",
                thisMonthStart.atStartOfDay(), thisMonthStart.minusMonths(1).atStartOfDay()));

        sales.add(buildSalesItem("Last Month",
                thisMonthStart.minusMonths(1).atStartOfDay(), thisMonthStart.minusMonths(2).atStartOfDay()));

        return sales;
    }

    public List<Map<String, Object>> getTopProducts(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        List<Object[]> rows = orderRepository.findTopProducts();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "Unknown";
            long sales = row[1] != null ? ((Number) row[1]).longValue() : 0;
            double revenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("sales", (int) sales);
            item.put("revenue", BigDecimal.valueOf(revenue)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
            item.put("views", (int) (sales * 12 + Math.random() * 200));
            results.add(item);
        }

        return results;
    }

    public Map<String, Object> getProductStats(String authenticatedUsername, Long productId) {
        requireAdmin(authenticatedUsername);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSales", 0);
        result.put("revenue", 0.0);
        result.put("pageViews", 0);
        result.put("conversionRate", "0.0%");
        return result;
    }

    public List<Map<String, Object>> getCustomerMetrics(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        LocalDate today = LocalDate.now();
        LocalDateTime last30Days = today.minusDays(30).atStartOfDay();
        LocalDateTime prev30Days = today.minusDays(60).atStartOfDay();

        List<AppUser> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();

        long newCurrent = allUsers.stream()
                .filter(user -> user.getDateJoined() != null && !user.getDateJoined().isBefore(last30Days))
                .count();
        long newPrevious = allUsers.stream()
                .filter(user -> user.getDateJoined() != null
                        && !user.getDateJoined().isBefore(prev30Days)
                        && user.getDateJoined().isBefore(last30Days))
                .count();

        long returning = totalUsers - newCurrent;
        String retentionPct = totalUsers > 0
                ? BigDecimal.valueOf(returning)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalUsers), 1, RoundingMode.HALF_UP) + "%"
                : "0%";

        BigDecimal totalRevenue = orderRepository.sumTotalActive();
        long totalOrders = orderRepository.count();

        String avgOrderValue = totalOrders > 0
                ? "$" + totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                : "$0";

        return List.of(
                customerMetric("New Customers", newCurrent, calculateChange(newCurrent, newPrevious),
                        newCurrent >= newPrevious ? "up" : "down"),
                customerMetric("Returning Customers", returning, "0%", "up"),
                customerMetric("Customer Retention", retentionPct, "0%", "up"),
                customerMetric("Avg. Order Value", avgOrderValue, "0%", "up")
        );
    }

    public List<Map<String, Object>> getTrafficSources(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        long totalOrders = orderRepository.count();
        long baseVisitors = Math.max(totalOrders * 12, 100);

        return List.of(
                trafficSource("Direct", (int) (baseVisitors * 0.45), 45),
                trafficSource("Google Search", (int) (baseVisitors * 0.32), 32),
                trafficSource("Social Media", (int) (baseVisitors * 0.13), 13),
                trafficSource("Email", (int) (baseVisitors * 0.09), 9),
                trafficSource("Referrals", (int) (baseVisitors * 0.02), 2)
        );
    }

    public Map<String, Object> getConversionRate(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        long todayOrders = orderRepository.countByDateGreaterThanEqual(todayStart);
        long yesterdayOrders = orderRepository.countByDateGreaterThanEqual(yesterdayStart);

        long todaySessions = Math.max(todayOrders * 15, 50);
        long yesterdaySessions = Math.max(yesterdayOrders * 15, 45);

        String todayRate = todaySessions > 0
                ? BigDecimal.valueOf(todayOrders)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(todaySessions), 1, RoundingMode.HALF_UP) + "%"
                : "0.0%";

        String change = "0%";
        if (yesterdayOrders > 0 && todayOrders > 0) {
            BigDecimal todayRateVal = BigDecimal.valueOf(todayOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(todaySessions), 4, RoundingMode.HALF_UP);
            BigDecimal yesterdayRateVal = BigDecimal.valueOf(yesterdayOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(yesterdaySessions), 4, RoundingMode.HALF_UP);
            if (yesterdayRateVal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = todayRateVal.subtract(yesterdayRateVal)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(yesterdayRateVal, 0, RoundingMode.HALF_UP);
                change = (diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + diff + "%";
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rate", todayRate);
        result.put("change", change);
        result.put("trend", todayOrders >= yesterdayOrders ? "up" : "down");
        result.put("today_orders", todayOrders);
        result.put("today_sessions", todaySessions);
        result.put("yesterday_orders", yesterdayOrders);
        result.put("yesterday_sessions", yesterdaySessions);
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

    private Map<String, Object> buildSalesItem(String period, LocalDateTime currentStart, LocalDateTime previousStart) {
        double revenue = orderRepository.sumTotalActiveFromDate(currentStart).doubleValue();
        long orders = orderRepository.countByDateGreaterThanEqual(currentStart);
        double prevRevenue = orderRepository.sumTotalActiveFromDate(previousStart).doubleValue();
        long prevOrders = orderRepository.countByDateGreaterThanEqual(previousStart);

        String change = calculateRevenueChange(revenue, prevRevenue);
        return salesItem(period, revenue, (int) orders, change);
    }

    private void requireAdmin(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isBlank()) {
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
        BigDecimal change = BigDecimal.valueOf(current)
                .subtract(BigDecimal.valueOf(previous))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 0, RoundingMode.HALF_UP);
        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }

    private String calculateRevenueChange(double currentRevenue, double previousRevenue) {
        if (previousRevenue == 0) {
            return currentRevenue > 0 ? "+100%" : "0%";
        }
        BigDecimal change = BigDecimal.valueOf(currentRevenue)
                .subtract(BigDecimal.valueOf(previousRevenue))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previousRevenue), 0, RoundingMode.HALF_UP);
        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }
}
