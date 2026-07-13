package com.hoang.backend.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentSucceededEvent extends ApplicationEvent {

    private final String orderId;
    private final java.math.BigDecimal amount;
    private final String paymentMethod;

    public PaymentSucceededEvent(Object source, String orderId, java.math.BigDecimal amount, String paymentMethod) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
