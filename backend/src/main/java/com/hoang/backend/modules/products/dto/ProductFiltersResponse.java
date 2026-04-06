package com.hoang.backend.modules.products.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductFiltersResponse(
        PriceRange price_range,
        List<ProductColorResponse> colors,
        List<String> storage_options
) {
    public record PriceRange(BigDecimal min_price, BigDecimal max_price) {
    }
}
