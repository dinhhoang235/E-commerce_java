package com.hoang.backend.modules.payments.service;

import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.constants.OrderStatus;
import com.hoang.backend.common.constants.PaymentStatus;
import com.hoang.backend.modules.payments.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StripeWebhookHandler {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;

    public void handleEvent(String payload) {
        if (blank(payload)) return;
        com.fasterxml.jackson.databind.JsonNode node;
        try {
            node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
        } catch (Exception e) { return; }

        String eventType = node.path("type").asText("");
        if (blank(eventType)) return;

        if ("checkout.session.completed".equals(eventType)) {
            String sessionId = node.path("data").path("object").path("id").asText(null);
            if (notBlank(sessionId)) {
                paymentTransactionRepository.findByStripeCheckoutId(sessionId).ifPresent(transaction -> {
                    Order order = transaction.getOrder();
                    order.setStatus(OrderStatus.PROCESSING);
                    order.setIsPaid(true);
                    order.setCheckoutUrl(null);
                    orderRepository.save(order);
                    transaction.setStatus(PaymentStatus.SUCCESS);
                    paymentTransactionRepository.save(transaction);
                });
            }
        }
    }

}
