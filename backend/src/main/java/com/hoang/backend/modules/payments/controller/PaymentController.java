package com.hoang.backend.modules.payments.controller;

import com.hoang.backend.modules.payments.dto.CreateCheckoutFromCartRequest;
import com.hoang.backend.modules.payments.dto.CreateCheckoutSessionRequest;
import com.hoang.backend.modules.payments.dto.PaymentSessionResponse;
import com.hoang.backend.modules.payments.dto.RefundRequest;
import com.hoang.backend.modules.payments.dto.VerifyPaymentRequest;
import com.hoang.backend.modules.payments.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(value = {"/create-checkout-session", "/create-checkout-session/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentSessionResponse> createCheckoutSession(
            Authentication authentication,
            @RequestBody CreateCheckoutSessionRequest request
    ) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(authentication.getName(), request.order_id()));
    }

    @PostMapping(value = {"/create-checkout-session-from-cart", "/create-checkout-session-from-cart/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentSessionResponse> createCheckoutSessionFromCart(
            Authentication authentication,
            @RequestBody CreateCheckoutFromCartRequest request
    ) {
        return ResponseEntity.ok(paymentService.createCheckoutSessionFromCart(authentication.getName(), request));
    }

    @PostMapping(value = {"/continue-payment", "/continue-payment/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentSessionResponse> continuePayment(
            Authentication authentication,
            @RequestBody CreateCheckoutSessionRequest request
    ) {
        return ResponseEntity.ok(paymentService.continuePayment(authentication.getName(), request.order_id()));
    }

    @PostMapping(value = {"/verify-payment", "/verify-payment/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> verifyPayment(
            Authentication authentication,
            @RequestBody VerifyPaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.verifyPaymentAndCreateOrder(authentication.getName(), request.session_id()));
    }

    @GetMapping(value = {"/status/{orderId}", "/status/{orderId}/"})
    public ResponseEntity<Map<String, Object>> paymentStatus(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.paymentStatus(authentication.getName(), orderId));
    }

    @PostMapping(value = {"/refund", "/refund/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refund(Authentication authentication, @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processFullRefund(authentication.getName(), request.order_id(), request.reason()));
    }

    @GetMapping(value = {"/refund-status/{orderId}", "/refund-status/{orderId}/"})
    public ResponseEntity<Map<String, Object>> refundStatus(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.refundStatus(authentication.getName(), orderId));
    }

    @PostMapping(value = {"/validate-cart-variants", "/validate-cart-variants/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateCartVariants(@RequestBody CreateCheckoutFromCartRequest request) {
        return ResponseEntity.ok(paymentService.validateCartVariants(request == null ? null : request.cart_items()));
    }

    @PostMapping(value = {"/webhook", "/webhook/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            HttpServletRequest request
    ) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes());
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
