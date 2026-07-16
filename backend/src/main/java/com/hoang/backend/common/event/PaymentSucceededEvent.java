package com.hoang.backend.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededEvent {

    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
}
