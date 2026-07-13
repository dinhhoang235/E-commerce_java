package com.hoang.backend.common.shipping;

import java.math.BigDecimal;

public class OvernightShippingStrategy implements ShippingCostStrategy {

    @Override
    public BigDecimal getCost() {
        return new BigDecimal("10.00");
    }

    @Override
    public String getLabel() {
        return "Overnight Shipping";
    }

    @Override
    public String getMethod() {
        return "overnight";
    }
}
