package com.hoang.backend.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingEventListener {

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[EVENT] Order created - orderId={}, userId={}, total={}",
                event.getOrderId(), event.getUserId(), event.getTotal());
    }

    @EventListener
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[EVENT] Payment succeeded - orderId={}, amount={}, method={}",
                event.getOrderId(), event.getAmount(), event.getPaymentMethod());
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[EVENT] Order status changed - orderId={} {} -> {}",
                event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
    }
}
