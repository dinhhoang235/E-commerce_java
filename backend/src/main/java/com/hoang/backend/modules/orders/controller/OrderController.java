package com.hoang.backend.modules.orders.controller;

import com.hoang.backend.modules.orders.dto.OrderCreateRequest;
import com.hoang.backend.modules.orders.dto.OrderHistoryResponse;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.OrderStatsResponse;
import com.hoang.backend.modules.orders.dto.OrderStatusUpdateRequest;
import com.hoang.backend.modules.orders.service.OrderService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<java.util.List<OrderResponse>> listMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.listMyOrders(authentication.getName()));
    }

    @GetMapping(value = {"/{orderId}", "/{orderId}/"})
    public ResponseEntity<OrderResponse> getMyOrder(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getMyOrder(authentication.getName(), orderId));
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication, @RequestBody(required = false) OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(authentication.getName(), request));
    }

    @PostMapping(value = {"/create-from-cart", "/create-from-cart/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createFromCart(Authentication authentication, @RequestBody(required = false) OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrderFromCart(authentication.getName(), request));
    }

    @PatchMapping(value = {"/{orderId}/status", "/{orderId}/status/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> updateStatusAsUser(
            Authentication authentication,
            @PathVariable String orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatusAsUser(authentication.getName(), orderId, request.status()));
    }

    @PostMapping(value = {"/{orderId}/cancel", "/{orderId}/cancel/"})
    public ResponseEntity<OrderResponse> cancel(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(authentication.getName(), orderId));
    }

    @GetMapping(value = {"/history", "/history/"})
    public ResponseEntity<OrderHistoryResponse> history(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(orderService.history(authentication.getName(), page, pageSize));
    }

    @GetMapping(value = {"/stats", "/stats/"})
    public ResponseEntity<OrderStatsResponse> stats(Authentication authentication) {
        return ResponseEntity.ok(orderService.myStats(authentication.getName()));
    }

    @GetMapping(value = {"/{orderId}/check-payment", "/{orderId}/check-payment/"})
    public ResponseEntity<Map<String, Object>> checkPayment(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.checkPaymentStatus(authentication.getName(), orderId));
    }
}
