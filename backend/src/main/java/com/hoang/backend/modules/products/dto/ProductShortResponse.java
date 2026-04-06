package com.hoang.backend.modules.products.dto;

public record ProductShortResponse(
        Long id,
        String name,
        String image,
        String description
) {
}
