package com.hoang.backend.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final String orderId;
    private final String userId;
    private final java.math.BigDecimal total;

    public OrderCreatedEvent(Object source, String orderId, String userId, java.math.BigDecimal total) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.total = total;
    }
}
