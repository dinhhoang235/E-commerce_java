package com.hoang.backend.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final String orderId;
    private final String previousStatus;
    private final String newStatus;

    public OrderStatusChangedEvent(Object source, String orderId, String previousStatus, String newStatus) {
        super(source);
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
