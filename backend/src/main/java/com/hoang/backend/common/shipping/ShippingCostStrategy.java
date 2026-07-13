package com.hoang.backend.common.shipping;

import java.math.BigDecimal;

public interface ShippingCostStrategy {
    BigDecimal getCost();
    String getLabel();
    String getMethod();
}
