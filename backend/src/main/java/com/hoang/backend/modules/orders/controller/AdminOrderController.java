package com.hoang.backend.modules.orders.controller;

import com.hoang.backend.modules.orders.dto.AdminOrderListResponse;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.OrderStatsResponse;
import com.hoang.backend.modules.orders.dto.OrderStatusUpdateRequest;
import com.hoang.backend.modules.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/admin")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<AdminOrderListResponse> list(
            Authentication authentication,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "customer", required = false) String customer,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(orderService.listAdminOrders(authentication.getName(), status, customer, limit));
    }

    @GetMapping(value = {"/{orderId}", "/{orderId}/"})
    public ResponseEntity<OrderResponse> retrieve(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.adminGetOrder(authentication.getName(), orderId));
    }

    @PatchMapping(value = {"/{orderId}", "/{orderId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> updateStatus(
            Authentication authentication,
            @PathVariable String orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.adminUpdateOrderStatus(authentication.getName(), orderId, request.status()));
    }

    @GetMapping(value = {"/stats", "/stats/"})
    public ResponseEntity<OrderStatsResponse> stats(Authentication authentication) {
        return ResponseEntity.ok(orderService.adminStats(authentication.getName()));
    }
}
