package com.hoang.backend.modules.products.controller;

import com.hoang.backend.modules.products.dto.ProductImageResponse;
import com.hoang.backend.modules.products.service.ProductImageService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<ProductImageResponse>> list(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getProductImages(productId));
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImageResponse>> uploadMultiple(
            Authentication authentication,
            @PathVariable Long productId,
            HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> files = multipartRequest.getFiles("imageFiles");
        return ResponseEntity.ok(productImageService.addMultipleImages(
                authentication.getName(), productId, files));
    }

    @PutMapping(value = {"/{imageId}", "/{imageId}/"})
    public ResponseEntity<ProductImageResponse> update(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam(value = "sort_order", required = false) Integer sortOrder,
            @RequestParam(value = "is_primary", required = false) Boolean isPrimary) {
        return ResponseEntity.ok(productImageService.updateImage(
                authentication.getName(), imageId, sortOrder, isPrimary));
    }

    @DeleteMapping(value = {"/{imageId}", "/{imageId}/"})
    public ResponseEntity<Map<String, String>> delete(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productImageService.deleteImage(authentication.getName(), imageId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
