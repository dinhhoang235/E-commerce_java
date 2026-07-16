package com.hoang.backend.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingEventHandler {

    @RabbitListener(queues = "order.created.queue")
    @RabbitHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[RABBIT] Order created - orderId={}, userId={}, total={}",
                event.getOrderId(), event.getUserId(), event.getTotal());
    }

    @RabbitListener(queues = "order.status.changed.queue")
    @RabbitHandler
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[RABBIT] Order status changed - orderId={} {} -> {}",
                event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
    }

    @RabbitListener(queues = "payment.succeeded.queue")
    @RabbitHandler
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[RABBIT] Payment succeeded - orderId={}, amount={}, method={}",
                event.getOrderId(), event.getAmount(), event.getPaymentMethod());
    }
}
