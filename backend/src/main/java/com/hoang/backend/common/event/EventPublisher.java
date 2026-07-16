package com.hoang.backend.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
    }

    public void publish(OrderStatusChangedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.status.changed", event);
    }

    public void publish(PaymentSucceededEvent event) {
        rabbitTemplate.convertAndSend("payment.exchange", "payment.succeeded", event);
    }
}
