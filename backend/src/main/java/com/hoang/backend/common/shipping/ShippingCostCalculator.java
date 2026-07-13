package com.hoang.backend.common.shipping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ShippingCostCalculator {

    private final Map<String, ShippingCostStrategy> strategies;
    private final ShippingCostStrategy defaultStrategy;

    public ShippingCostCalculator(List<ShippingCostStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getMethod().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
        this.defaultStrategy = new StandardShippingStrategy();
    }

    public BigDecimal getCost(String shippingMethod) {
        String normalized = normalize(shippingMethod);
        return strategies.getOrDefault(normalized, defaultStrategy).getCost();
    }

    public String getLabel(String shippingMethod) {
        String normalized = normalize(shippingMethod);
        return strategies.getOrDefault(normalized, defaultStrategy).getLabel();
    }

    private String normalize(String shippingMethod) {
        if (shippingMethod == null || shippingMethod.isBlank()) {
            return "standard";
        }
        return shippingMethod.toLowerCase(Locale.ROOT).trim();
    }
}
