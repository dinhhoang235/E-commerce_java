package com.hoang.backend.modules.payments.service;

import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.shipping.ShippingCostCalculator;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeSessionService {

    private final OrderItemRepository orderItemRepository;
    private final ShippingCostCalculator shippingCostCalculator;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public Session createCheckoutSession(Order order) {
        configureStripe();
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment/cancel")
                .putMetadata("order_id", order.getId())
                .putMetadata("user_id", String.valueOf(order.getUser().getId()))
                .setCustomerEmail(order.getUser().getEmail());

        for (OrderItem item : items) {
            String name = safe(item.getProductVariant().getProduct().getName());
            String description = safe(item.getProductVariant().getProduct().getDescription());
            long amountInCents = item.getPrice().multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP).longValue();

            SessionCreateParams.LineItem.PriceData.ProductData.Builder productBuilder =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(name.isBlank() ? "Product" : name);
            if (notBlank(description)) {
                productBuilder.setDescription(description.length() > 500 ? description.substring(0, 500) : description);
            }

            builder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) (item.getQuantity() == null ? 1 : item.getQuantity()))
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(productBuilder.build())
                            .build())
                    .build());
        }

        BigDecimal shippingCost = shippingCost(order.getShippingMethod());
        if (shippingCost.compareTo(BigDecimal.ZERO) > 0) {
            long shippingCents = shippingCost.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
            builder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(shippingCents)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(shippingMethodLabel(order.getShippingMethod()))
                                    .setDescription(shippingMethodLabel(order.getShippingMethod()) + " delivery")
                                    .build())
                            .build())
                    .build());
        }

        try { return Session.create(builder.build()); }
        catch (StripeException exception) { throw new IllegalArgumentException("Payment processing error: " + exception.getMessage()); }
    }

    public Refund createRefund(String paymentIntent, BigDecimal amount, String orderId, Long userId, String reason) {
        configureStripe();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntent).setAmount(amountInCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("order_id", orderId).putMetadata("user_id", String.valueOf(userId))
                .putMetadata("reason", reason).putMetadata("refund_type", "full_refund").build();
        try { return Refund.create(params); }
        catch (StripeException exception) { throw new IllegalArgumentException("Refund processing failed: " + exception.getMessage()); }
    }

    public Session retrieveSession(String sessionId) {
        configureStripe();
        try { return Session.retrieve(sessionId); }
        catch (StripeException exception) { throw new IllegalArgumentException("Invalid payment session"); }
    }

    public boolean isSessionOpen(String sessionId) {
        if (blank(sessionId)) return false;
        try { return "open".equalsIgnoreCase(retrieveSession(sessionId).getStatus()); }
        catch (Exception exception) { return false; }
    }

    private void configureStripe() {
        if (blank(stripeSecretKey)) throw new IllegalArgumentException("Stripe secret key is not configured");
        Stripe.apiKey = stripeSecretKey;
    }

    private BigDecimal shippingCost(String shippingMethod) { return shippingCostCalculator.getCost(shippingMethod); }
    private String shippingMethodLabel(String shippingMethod) { return shippingCostCalculator.getLabel(shippingMethod); }

}
