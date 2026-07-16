package com.hoang.backend.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

    private String orderId;
    private String previousStatus;
    private String newStatus;
}
