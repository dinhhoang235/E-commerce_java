package com.hoang.backend.common.shipping;

import java.math.BigDecimal;

public class ExpressShippingStrategy implements ShippingCostStrategy {

    @Override
    public BigDecimal getCost() {
        return new BigDecimal("5.00");
    }

    @Override
    public String getLabel() {
        return "Express Shipping";
    }

    @Override
    public String getMethod() {
        return "express";
    }
}
