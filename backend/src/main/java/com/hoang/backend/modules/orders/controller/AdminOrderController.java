package com.hoang.backend.modules.orders.controller;

import com.hoang.backend.common.dto.PaginatedResponse;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.OrderStatsResponse;
import com.hoang.backend.modules.orders.dto.OrderStatusUpdateRequest;
import com.hoang.backend.modules.orders.service.OrderCommandService;
import com.hoang.backend.modules.orders.service.OrderQueryService;
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

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<PaginatedResponse<OrderResponse>> list(
            Authentication authentication,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "customer", required = false) String customer,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(orderQueryService.listAdminOrders(authentication.getName(), status, customer, page, pageSize));
    }

    @GetMapping(value = {"/{orderId}", "/{orderId}/"})
    public ResponseEntity<OrderResponse> retrieve(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(orderQueryService.adminGetOrder(authentication.getName(), orderId));
    }

    @PatchMapping(value = {"/{orderId}", "/{orderId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> updateStatus(
            Authentication authentication,
            @PathVariable String orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderCommandService.adminUpdateOrderStatus(authentication.getName(), orderId, request.status()));
    }

    @GetMapping(value = {"/stats", "/stats/"})
    public ResponseEntity<OrderStatsResponse> stats(Authentication authentication) {
        return ResponseEntity.ok(orderQueryService.adminStats(authentication.getName()));
    }
}
