package com.hoang.backend.modules.products.controller;

import com.hoang.backend.modules.products.dto.ProductColorResponse;
import com.hoang.backend.modules.products.service.ProductService;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-colors")
public class ProductColorController {

    private final ProductService productService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<ProductColorResponse>> list() {
        return ResponseEntity.ok(productService.listColors());
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductColorResponse> create(Authentication authentication, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(productService.createColor(authentication.getName(), payload));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductColorResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(productService.updateColor(authentication.getName(), id, payload));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, String>> delete(Authentication authentication, @PathVariable Long id) {
        productService.deleteColor(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
