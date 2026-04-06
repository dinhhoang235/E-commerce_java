package com.hoang.backend.modules.products.controller;

import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.dto.StockUpdateRequest;
import com.hoang.backend.modules.products.service.ProductService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-variants")
public class ProductVariantController {

    private final ProductService productService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<ProductVariantResponse>> list(@RequestParam(name = "product_id", required = false) Long productId) {
        return ResponseEntity.ok(productService.listVariants(Optional.ofNullable(productId)));
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<ProductVariantResponse> retrieve(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getVariant(id));
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductVariantResponse> create(Authentication authentication, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(productService.createVariant(authentication.getName(), payload));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductVariantResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(productService.updateVariant(authentication.getName(), id, payload));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, String>> delete(Authentication authentication, @PathVariable Long id) {
        productService.deleteVariant(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @PostMapping(value = {"/{id}/reduce_stock", "/{id}/reduce_stock/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductVariantResponse> reduceStock(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.reduceStock(authentication.getName(), id, request.quantity()));
    }

    @PostMapping(value = {"/{id}/increase_stock", "/{id}/increase_stock/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductVariantResponse> increaseStock(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.increaseStock(authentication.getName(), id, request.quantity()));
    }
}
