package com.hoang.backend.modules.adminpanel.controller;

/**
 * Controller cho các endpoint admin tương thích contract của backend Django cũ.
 */

import com.hoang.backend.modules.adminpanel.dto.AdminLoginRequest;
import com.hoang.backend.modules.adminpanel.dto.AdminLoginResponse;
import com.hoang.backend.modules.adminpanel.service.AdminPanelService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminPanelController {

    private final AdminPanelService adminPanelService;

    @PostMapping(value = {"/login", "/login/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminPanelService.login(request));
    }

    @GetMapping(value = {"/analytics/sales", "/analytics/sales/"})
    public ResponseEntity<List<Map<String, Object>>> sales(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getSalesAnalytics(authentication.getName()));
    }

    @GetMapping(value = {"/analytics/products", "/analytics/products/"})
    public ResponseEntity<List<Map<String, Object>>> products(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getTopProducts(authentication.getName()));
    }

    @GetMapping(value = {"/analytics/products/{productId}", "/analytics/products/{productId}/"})
    public ResponseEntity<Map<String, Object>> productStats(Authentication authentication, @PathVariable Long productId) {
        return ResponseEntity.ok(adminPanelService.getProductStats(authentication.getName(), productId));
    }

    @GetMapping(value = {"/analytics/customers", "/analytics/customers/"})
    public ResponseEntity<List<Map<String, Object>>> customers(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getCustomerMetrics(authentication.getName()));
    }

    @GetMapping(value = {"/analytics/traffic", "/analytics/traffic/"})
    public ResponseEntity<List<Map<String, Object>>> traffic(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getTrafficSources(authentication.getName()));
    }

    @GetMapping(value = {"/analytics/conversion", "/analytics/conversion/"})
    public ResponseEntity<Map<String, Object>> conversion(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getConversionRate(authentication.getName()));
    }

    @GetMapping(value = {"/analytics/dashboard", "/analytics/dashboard/"})
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getDashboard(authentication.getName()));
    }

    @GetMapping(value = {"/payments", "/payments/"})
    public ResponseEntity<List<Map<String, Object>>> paymentTransactions(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getPaymentTransactions(authentication.getName()));
    }

    @GetMapping(value = {"/payments/stats", "/payments/stats/"})
    public ResponseEntity<Map<String, Object>> paymentStats(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getPaymentStats(authentication.getName()));
    }

    @GetMapping(value = {"/settings", "/settings/"})
    public ResponseEntity<Map<String, Object>> settings(Authentication authentication) {
        return ResponseEntity.ok(adminPanelService.getStoreSettings(authentication.getName()));
    }

    @PutMapping(value = {"/settings", "/settings/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateSettings(Authentication authentication, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(adminPanelService.updateStoreSettings(authentication.getName(), payload, null));
    }

    @PatchMapping(value = {"/settings/{section}", "/settings/{section}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateSettingsSection(
            Authentication authentication,
            @PathVariable String section,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(adminPanelService.updateStoreSettings(authentication.getName(), payload, section));
    }
}
