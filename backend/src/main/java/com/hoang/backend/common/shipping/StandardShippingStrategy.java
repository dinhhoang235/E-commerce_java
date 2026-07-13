package com.hoang.backend.common.shipping;

import java.math.BigDecimal;

public class StandardShippingStrategy implements ShippingCostStrategy {

    @Override
    public BigDecimal getCost() {
        return BigDecimal.ZERO;
    }

    @Override
    public String getLabel() {
        return "Standard Shipping";
    }

    @Override
    public String getMethod() {
        return "standard";
    }
}
