package com.hoang.backend.modules.products.controller;

import com.hoang.backend.common.RequestPayloadReader;
import com.hoang.backend.modules.products.dto.CategoryResponse;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final ProductService productService;
    private final RequestPayloadReader payloadReader;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(productService.listCategories());
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<CategoryResponse> retrieve(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getCategory(id));
    }

    @PostMapping(value = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> create(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(productService.createCategory(authentication.getName(), payload, imageFile));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> update(Authentication authentication, @PathVariable Long id, HttpServletRequest request)
            throws IOException {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(productService.updateCategory(authentication.getName(), id, payload, imageFile));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, String>> delete(Authentication authentication, @PathVariable Long id) {
        productService.deleteCategory(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    private MultipartFile readImageFile(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            return multipartRequest.getFile("imageFile");
        }
        return null;
    }
}
