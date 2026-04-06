package com.hoang.backend.modules.products.dto;

public record ProductColorResponse(
        Long id,
        String name,
        String hex_code
) {
}
