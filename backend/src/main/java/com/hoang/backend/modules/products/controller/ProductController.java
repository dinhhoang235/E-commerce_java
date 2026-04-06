package com.hoang.backend.modules.products.controller;

import com.hoang.backend.common.RequestPayloadReader;
import com.hoang.backend.modules.products.dto.ProductFiltersResponse;
import com.hoang.backend.modules.products.dto.ProductResponse;
import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final RequestPayloadReader payloadReader;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<ProductResponse>> list(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(productService.listProducts(queryParams));
    }

    @GetMapping(value = {"/top_sellers", "/top_sellers/"})
    public ResponseEntity<List<ProductResponse>> topSellers() {
        return ResponseEntity.ok(productService.getTopSellers());
    }

    @GetMapping(value = {"/new_arrivals", "/new_arrivals/"})
    public ResponseEntity<List<ProductResponse>> newArrivals() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping(value = {"/personalized", "/personalized/"})
    public ResponseEntity<List<ProductResponse>> personalized(@RequestParam(name = "categories", required = false) List<Long> categoryIds) {
        return ResponseEntity.ok(productService.getPersonalized(categoryIds == null ? List.of() : categoryIds));
    }

    @GetMapping(value = {"/filters", "/filters/"})
    public ResponseEntity<ProductFiltersResponse> filters(@RequestParam(name = "category__slug", required = false) String categorySlug) {
        return ResponseEntity.ok(productService.getFilters(categorySlug));
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<ProductResponse> retrieve(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping(value = {"/{id}/variants", "/{id}/variants/"})
    public ResponseEntity<List<ProductVariantResponse>> variants(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductVariants(id));
    }

    @GetMapping(value = {"/{id}/recommendations", "/{id}/recommendations/"})
    public ResponseEntity<List<ProductResponse>> recommendations(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getRecommendations(id));
    }

    @PostMapping(value = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductResponse> create(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(productService.createProduct(authentication.getName(), payload, imageFile));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductResponse> update(Authentication authentication, @PathVariable Long id, HttpServletRequest request)
            throws IOException {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(productService.updateProduct(authentication.getName(), id, payload, imageFile));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, String>> delete(Authentication authentication, @PathVariable Long id) {
        productService.deleteProduct(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    private MultipartFile readImageFile(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            return multipartRequest.getFile("imageFile");
        }
        return null;
    }
}
